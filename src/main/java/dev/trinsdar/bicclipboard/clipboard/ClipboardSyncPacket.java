package dev.trinsdar.bicclipboard.clipboard;

import dev.trinsdar.bicclipboard.BiCClipboardData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public record ClipboardSyncPacket(ClipboardContent content) {
    public static void encode(ClipboardSyncPacket packet, FriendlyByteBuf buffer) {
        packet.content.encode(buffer);
    }

    public static ClipboardSyncPacket decode(FriendlyByteBuf buffer) {
        ClipboardContent clipboardContent = ClipboardContent.decode(buffer);
        return new ClipboardSyncPacket(clipboardContent);
    }
    public static void handle(ClipboardSyncPacket packet, @Nonnull Supplier<Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player sender = ctx.get().getSender();
            if (sender != null) {
                ItemStack stack = sender.getMainHandItem();
                if (!stack.is(BiCClipboardData.CLIPBOARD_ITEM.get())){
                    stack = sender.getOffhandItem();
                    if (!stack.is(BiCClipboardData.CLIPBOARD_ITEM.get())) return;
                }
                stack.getOrCreateTag().put("clipboardContent", packet.content().serialize());
            }
        });
    }
}
