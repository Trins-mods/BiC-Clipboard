package dev.trinsdar.bicclipboard;

import dev.trinsdar.bcclipboard.clipboard.ClipboardBlock;
import dev.trinsdar.bcclipboard.clipboard.ClipboardBlockEntity;
import dev.trinsdar.bcclipboard.clipboard.ClipboardItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BCClipboardData {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, BCClipboard.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, BCClipboard.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, BCClipboard.ID);

    public static final RegistryObject<Block> CLIPBOARD = BLOCKS.register("clipboard", ClipboardBlock::new);
    public static final RegistryObject<Item> CLIPBOARD_ITEM = ITEMS.register("clipboard", ClipboardItem::new);
    public static final RegistryObject<BlockEntityType<ClipboardBlockEntity>> CLIPBOARD_BLOCK_ENTITY = BLOCK_ENTITIES.register("clipboard", () -> BlockEntityType.Builder.of(ClipboardBlockEntity::new, CLIPBOARD.get()).build(null));
    public static final CreativeModeTab CLIPBOARD_TAB = new CreativeModeTab(BCClipboard.ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(CLIPBOARD_ITEM.get());
        }
    };
    public static void init(){
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
    }
}
