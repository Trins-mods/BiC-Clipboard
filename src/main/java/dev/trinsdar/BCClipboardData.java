package dev.trinsdar;

import dev.trinsdar.clipboard.ClipboardBlock;
import dev.trinsdar.clipboard.ClipboardBlockEntity;
import dev.trinsdar.clipboard.ClipboardItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCClipboardData {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCClipboard.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCClipboard.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BCClipboard.ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BCClipboard.ID);

    public static final RegistryObject<Block> CLIPBOARD = BLOCKS.register("clipboard", ClipboardBlock::new);
    public static final RegistryObject<Item> CLIPBOARD_ITEM = ITEMS.register("clipboard", ClipboardItem::new);
    public static final RegistryObject<BlockEntityType<ClipboardBlockEntity>> CLIPBOARD_BLOCK_ENTITY = BLOCK_ENTITIES.register("clipboard", () -> BlockEntityType.Builder.of(ClipboardBlockEntity::new, CLIPBOARD.get()).build(null));
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("bc_clipboard", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + BCClipboard.ID))
            .icon(() -> CLIPBOARD_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(CLIPBOARD_ITEM.get());
            }).build());
    public static void init(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
