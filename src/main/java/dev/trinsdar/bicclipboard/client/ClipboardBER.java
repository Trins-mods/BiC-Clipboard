package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.trinsdar.bcclipboard.BCClipboardUtils;
import dev.trinsdar.bcclipboard.clipboard.CheckboxState;
import dev.trinsdar.bcclipboard.clipboard.ClipboardBlockEntity;
import dev.trinsdar.bcclipboard.clipboard.ClipboardContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class ClipboardBER implements BlockEntityRenderer<ClipboardBlockEntity> {

    @Override
    public void render(ClipboardBlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        stack.pushPose();

        ClientUtil.setupCenteredBER(stack, blockEntity);
        stack.mulPose(Vector3f.XP.rotationDegrees(180));
        RenderSystem.enableDepthTest();
        stack.translate(-0.25, -0.25, 0.4375);
        stack.translate(0, 0, -1 / 1024d);

        /*
         * Scale the rendering plane from texture-space (pixels) to world-space (block units).
         * - X and Y are scaled by 1/256 to convert pixel coordinates to world units (1 unit = 256 pixels).
         * - The Z scale is negative and small (-0.01) to flatten the rendered elements and push them slightly "behind"
         *   the origin plane in a way that helps prevent z-fighting.
         *
         * The negative Z scale also effectively flips the Z axis, allowing later child elements to be offset by small positive
         * values (e.g., z + 0.002) so they render visually *on top* of this base layer.
         */
        stack.scale(1 / 256f, 1 / 256f, -0.01F);

        ClipboardContent data = blockEntity.getContent();
        if (data != null) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            RenderSystem.setShaderTexture(0, BCClipboardUtils.BACKGROUND_BLOCK);
            GuiComponent.blit(stack, 0, 0, 0, 0, 128, 148, 256, 256);

            drawLayeredText(stack, buffer, font, data.title(), 29, 2, 72);
            ClipboardContent.Page page = data.pages().get(data.active());
            for (int i = 0; i < ClipboardContent.MAX_LINES; i++) {
                int y = 15 * i + 14;
                int textY = 15 * i + 16;
                CheckboxState state = page.checkboxes().get(i);
                if (state == CheckboxState.CHECK) {
                    drawSprite(stack, BCClipboardUtils.CHECK_TEXTURE, 2, y, 14, 14);
                } else if (state == CheckboxState.X) {
                    drawSprite(stack, BCClipboardUtils.X_TEXTURE, 2, y, 14, 14);
                }
                drawLayeredText(stack, buffer, font, page.lines().get(i), 17, textY, 109);
            }
        }

        // Restore depth testing state (redundant but safe)
        RenderSystem.enableDepthTest();
        stack.popPose();
    }

    /**
     * Wraps a rendering operation in a safe Z offset.
     * This is useful to lift plane elements slightly above a base layer (e.g., text above background)
     * without risking z-fighting.
     *
     * @param stack      Matrix stack used for current rendering transform
     * @param zOffset    Z depth value to push forward
     * @param renderCall A lambda or callback containing the drawing logic
     */
    private void withZOffset(PoseStack stack, double zOffset, ZOffsetCallback renderCall) {
        stack.pushPose();
        stack.translate(0, 0, zOffset); // Isolate the Z offset to this operation
        renderCall.add();
        stack.popPose();
    }

    /**
     * Draws a sprite on the plane layer with depth testing and Z offset.
     * Prevents icons from clipping into or behind other elements.
     */
    private void drawSprite(PoseStack stack, ResourceLocation texture, int x, int y, int w, int h) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableDepthTest(); // Always ensure depth test is active when rendering world planes

        withZOffset(stack, 0.002, () ->
                GuiComponent.blit(stack, x, y, 0, 0, w, h, w, h)
        );
    }

    /**
     * Renders a single line of text with a fixed Z offset to ensure it appears above the background
     * and is not affected by z-fighting or render order issues.
     */
    private void drawLayeredText(PoseStack stack, MultiBufferSource buffer, Font font, String text, int x, int y, int width) {
        String visible = font.plainSubstrByWidth(text, width);
        if (visible.isEmpty()) return;

        withZOffset(stack, 0.002, () ->
                font.drawInBatch(visible, x, y, 0, false, stack.last().pose(), buffer, false, 0, 0xF000F0)
        );
    }

    public interface ZOffsetCallback {
        void add();
    }
}
