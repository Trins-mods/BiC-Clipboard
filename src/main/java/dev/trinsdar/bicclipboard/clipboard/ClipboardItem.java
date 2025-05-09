package dev.trinsdar.bicclipboard.clipboard;

import dev.trinsdar.bicclipboard.BiCClipboardData;
import dev.trinsdar.bicclipboard.client.ClientUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class ClipboardItem  extends BlockItem {
    public ClipboardItem() {
        super(BiCClipboardData.CLIPBOARD.get(), new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            ClientUtil.openClipboardScreen(stack);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && context.getPlayer().isSecondaryUseActive() ? super.useOn(context) : InteractionResult.PASS;
    }
}
