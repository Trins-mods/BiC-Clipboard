package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.trinsdar.bicclipboard.clipboard.ClipboardBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class ClipboardBER implements BlockEntityRenderer<ClipboardBlockEntity> {
    @Override
    public void render(ClipboardBlockEntity blockEntity, float v, PoseStack stack, MultiBufferSource buffer, int i, int i1) {
        stack.pushPose();
        ClientUtil.setupCenteredBER(stack, blockEntity);
        stack.mulPose(Axis.XP.rotationDegrees(180));
        stack.translate(-0.25, -0.25, 0.4375);
        stack.translate(0, 0, -1 / 1024d);
        float scale = 1 / 256f;
        stack.scale(scale, scale, 0);
        ClipboardReadOnlyRenderer.render(stack, buffer, blockEntity.getContent(), 128, 148);
        stack.popPose();
    }
}
