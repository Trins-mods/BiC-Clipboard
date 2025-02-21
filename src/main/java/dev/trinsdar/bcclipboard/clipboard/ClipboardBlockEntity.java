package dev.trinsdar.bcclipboard.clipboard;

import dev.trinsdar.bcclipboard.BCClipboardData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ClipboardBlockEntity extends BlockEntity {
    private ClipboardContent content = ClipboardContent.DEFAULT;

    public ClipboardBlockEntity(BlockPos pos, BlockState blockState) {
        super(BCClipboardData.CLIPBOARD_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        content = ClipboardContent.deserialize(tag.getCompound("content"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("content", content.serialize());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        updateTag.put("content", content.serialize());
        return updateTag;
    }

    public ClipboardContent getContent() {
        return content;
    }

    public void setContent(ClipboardContent content) {
        this.content = content;
        setChanged();
    }
}
