package dev.trinsdar.bicclipboard;

import com.mojang.logging.LogUtils;
import dev.trinsdar.bicclipboard.client.ClientHandler;
import dev.trinsdar.bicclipboard.clipboard.ClipboardSyncPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

@Mod(BiCClipboard.ID)
public class BiCClipboard {

    public static final String ID = "bc_clipboard";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private int currMessageId = 0;

    public BiCClipboard() {
        BiCClipboardData.init();
        INSTANCE.registerMessage(currMessageId++, ClipboardSyncPacket.class, ClipboardSyncPacket::encode, ClipboardSyncPacket::decode, ClipboardSyncPacket::handle);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientHandler::init);
    }
}
