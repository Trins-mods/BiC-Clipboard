package dev.trinsdar.bicclipboard.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ClientUtil {
    public static void openClipboardScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new ClipboardScreen(stack));
    }

    public static void setupCenteredBER(PoseStack stack, BlockEntity blockEntity) {
        stack.translate(0.5, 0.5, 0.5);
        BlockState state = blockEntity.getBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            stack.mulPose(Vector3f.YP.rotationDegrees(switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case SOUTH -> 0;
                case EAST -> 90;
                default -> 180;
                case WEST -> 270;
            }));
        }
    }
}
