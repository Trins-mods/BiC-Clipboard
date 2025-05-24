package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.trinsdar.bicclipboard.BiCClipboardUtils;
import dev.trinsdar.bicclipboard.clipboard.CheckboxState;
import dev.trinsdar.bicclipboard.clipboard.ClipboardBlockEntity;
import dev.trinsdar.bicclipboard.clipboard.ClipboardContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ClipboardBER implements BlockEntityRenderer<ClipboardBlockEntity> {
    @Override
    public void render(ClipboardBlockEntity blockEntity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light, int overlay) {
        stack.pushPose();
        ClientUtil.setupCenteredBER(stack, blockEntity);
        stack.mulPose(Axis.XP.rotationDegrees(180));
        RenderSystem.enableDepthTest();
        stack.translate(-0.25, -0.25, 0.4375);
        stack.translate(0, 0, -1 / 1024d);
        float scale = 1 / 256f;
        /*
         * Scale the rendering plane from texture-space (pixels) to world-space (block units).
         * - X and Y are scaled by 1/256 to convert pixel coordinates to world units (1 unit = 256 pixels).
         * - The Z scale is negative and small (-0.01) to flatten the rendered elements and push them slightly "behind"
         *   the origin plane in a way that helps prevent z-fighting.
         *
         * The negative Z scale also effectively flips the Z axis, allowing later child elements to be offset by small positive
         * values (e.g., z + 0.002) so they render visually *on top* of this base layer.
         */
        stack.scale(scale, scale, -0.01F);
        ClipboardContent data = blockEntity.getContent();
        if (data != null) {
            Minecraft mc = Minecraft.getInstance();
            Font font = mc.font;
            blit(stack, BiCClipboardUtils.BACKGROUND_BLOCK, 0, 0, 0, 0, 128, 148, 256, 256);

            drawLayeredText(stack, buffer, font, data.title(), 29, 2, 72);
            ClipboardContent.Page page = data.pages().get(data.active());
            for (int j = 0; j < ClipboardContent.MAX_LINES; j++) {
                int y = 15 * j + 14;
                int textY = 15 * j + 16;
                CheckboxState state = page.checkboxes().get(j);
                if (state == CheckboxState.CHECK) {
                    drawSprite(stack, BiCClipboardUtils.CHECK_TEXTURE, 2, y, 14, 14);
                } else if (state == CheckboxState.X) {
                    drawSprite(stack, BiCClipboardUtils.X_TEXTURE, 2, y, 14, 14);
                }
                drawLayeredText(stack, buffer, font, page.lines().get(j), 17, textY, 109);
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

    private void drawLayeredText(PoseStack stack, MultiBufferSource buffer, Font font, String text, float x, float y, int width) {
        String visibleText = font.plainSubstrByWidth(text, width);
        if (visibleText.isEmpty()) return;
        withZOffset(stack, 0.002, () ->
                font.drawInBatch(visibleText, x, y, 0, false, stack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT, font.isBidirectional())
        );
    }

    private void drawSprite(PoseStack pose, ResourceLocation location, int x, int y, int width, int height) {
        RenderSystem.enableDepthTest();
        withZOffset(pose, 0.002, () ->
                blit(pose, location, x, y, 0, 0, width, height, width, height)
        );
    }

    private static void blit(PoseStack pose, ResourceLocation atlasLocation, float x, float y, float uOffset, float vOffset, float uWidth, float vHeight, float textureWidth, float textureHeight) {
        float minU = uOffset / textureWidth;
        float maxU = (uOffset + uWidth) / textureWidth;
        float minV = vOffset / textureHeight;
        float maxV = (vOffset + vHeight) / textureHeight;
        innerBlit(pose, atlasLocation, x, x + uWidth, y, y + vHeight, minU, maxU, minV, maxV);
    }

    private static void innerBlit(PoseStack pose, ResourceLocation atlasLocation, float x1, float x2, float y1, float y2, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture(0, atlasLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f matrix4f = pose.last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bb.vertex(matrix4f, x1, y1, 0).uv(minU, minV).endVertex();
        bb.vertex(matrix4f, x1, y2, 0).uv(minU, maxV).endVertex();
        bb.vertex(matrix4f, x2, y2, 0).uv(maxU, maxV).endVertex();
        bb.vertex(matrix4f, x2, y1, 0).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(bb.end());
    }

    public interface ZOffsetCallback {
        void add();
    }
}
