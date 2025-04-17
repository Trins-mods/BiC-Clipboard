package dev.trinsdar.bicclipboard.client;

import dev.trinsdar.bicclipboard.BiCClipboardData;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientHandler {
    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BiCClipboardData.CLIPBOARD_BLOCK_ENTITY.get(), $ -> new ClipboardBER());
    }

    public static void init(){
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientHandler::registerRenderers);
    }
}
