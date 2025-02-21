package dev.trinsdar.bcclipboard.clipboard;

import dev.trinsdar.bcclipboard.BCClipboardData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

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

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public ClipboardContent getContent() {
        return content;
    }

    public void setContent(ClipboardContent content) {
        this.content = content;
        setChanged();
        BlockState state = getLevel().getBlockState(getBlockPos());
        getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
    }
}
