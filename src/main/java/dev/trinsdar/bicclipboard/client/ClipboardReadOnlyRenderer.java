package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import dev.trinsdar.bcclipboard.BCClipboard;
import dev.trinsdar.bcclipboard.clipboard.CheckboxState;
import dev.trinsdar.bcclipboard.clipboard.ClipboardContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds methods for rendering clipboard contents in a read-only manner. A lot of code in here is boiled-down code from GuiGraphics.
 */
@SuppressWarnings("SameParameterValue")
public final class ClipboardReadOnlyRenderer {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(BiCClipboard.ID, "textures/gui/clipboard_block.png");
    static final ResourceLocation CHECK_TEXTURE = new ResourceLocation(BiCClipboard.ID,"textures/gui/check.png");
    static final ResourceLocation X_TEXTURE = new ResourceLocation(BiCClipboard.ID,"textures/gui/x.png");

    public static void render(PoseStack pose, MultiBufferSource bufferSource, ClipboardContent data, int width, int height) {
        pose.pushPose();
        RenderSystem.enableDepthTest();
        ClipboardScreen.drawTexture(pose, BACKGROUND, 0, 0, 0, 0, width, height, 256, 256);
        RenderSystem.disableDepthTest();
        drawText(pose, bufferSource, data.title(), 29, 2, 72, 8);
        ClipboardContent.Page page = data.pages().get(data.active());
        for (int i = 0; i < ClipboardContent.MAX_LINES; i++) {
            CheckboxState state = page.checkboxes().get(i);
            if (state == CheckboxState.CHECK) {
                blitSprite(pose, CHECK_TEXTURE, 2, 15 * i + 14, 14, 14);
            } else if (state == CheckboxState.X) {
                blitSprite(pose, X_TEXTURE, 2, 15 * i + 14, 14, 14);
            }
            drawText(pose, bufferSource, page.lines().get(i), 17, 15 * i + 16, 109, 8);
        }
        pose.popPose();
    }

    private static void drawText(PoseStack pose, MultiBufferSource bufferSource, String text, float x, float y, int width, int height) {
        Font font = Minecraft.getInstance().font;
        String visibleText = font.plainSubstrByWidth(text, width);
        if (visibleText.isEmpty()) return;
        font.drawInBatch(visibleText, x, y, 0, false, pose.last().pose(), bufferSource, false, 0, LightTexture.FULL_BRIGHT, font.isBidirectional());
    }

    private static void blitSprite(PoseStack pose, ResourceLocation location, int x, int y, int width, int height) {
        ClipboardScreen.drawTexture(pose, location, x, y, 0, 0, width, height, width, height);
    }
}
