package dev.trinsdar.bicclipboard.clipboard;

import dev.trinsdar.bicclipboard.BiCClipboardUtils;
import dev.trinsdar.bicclipboard.clipboard.ClipboardContent.Page;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class ClipboardBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SHAPE = BiCClipboardUtils.combine(
            Shapes.box(0.1875, 0.125, 0.9375, 0.8125, 0.8125, 1),
            Shapes.box(0.4375, 0.8125, 0.9375, 0.5625, 0.875, 1));
    private static final VoxelShape EAST_SHAPE = BiCClipboardUtils.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_90);
    private static final VoxelShape SOUTH_SHAPE = BiCClipboardUtils.rotate(NORTH_SHAPE, Rotation.CLOCKWISE_180);
    private static final VoxelShape WEST_SHAPE = BiCClipboardUtils.rotate(NORTH_SHAPE, Rotation.COUNTERCLOCKWISE_90);
    public ClipboardBlock() {
        super(BlockBehaviour.Properties.of().instabreak().sound(SoundType.WOOD).ignitedByLava());
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ClipboardBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            default -> NORTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (level.getBlockEntity(pos) instanceof ClipboardBlockEntity clipboard) {
            CompoundTag clipboardTag = stack.getTagElement("clipboardContent");
            ClipboardContent clipboardContent = clipboardTag != null ? ClipboardContent.deserialize(clipboardTag) : ClipboardContent.DEFAULT;
            clipboard.setContent(clipboardContent);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, Builder params) {
        List<ItemStack> drops1 = super.getDrops(state, params);
        BlockEntity entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof ClipboardBlockEntity clipboard && !drops1.isEmpty()) {
            ItemStack stack = drops1.get(0);
            CompoundTag clipboardTag = clipboard.getContent().serialize();
            stack.getOrCreateTag().put("clipboardContent", clipboardTag);
        }
        return drops1;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Vec3 vec = hit.getLocation();
        double x = hit.getDirection().getAxis() == Direction.Axis.Z ?  vec.x() - hit.getBlockPos().getX() : vec.z() - hit.getBlockPos().getZ();
        double y = vec.y() - hit.getBlockPos().getY();
        if (hand == InteractionHand.MAIN_HAND) {
            if (player.isCrouching()) {
                dropResources(state, level, pos, level.getBlockEntity(pos));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            boolean opposite = hit.getDirection().get2DDataValue() > 1;
            if (hit.getDirection() == state.getValue(FACING) && ((x < 0.73 && x > 0.697 && opposite) || (x < 0.30 && x > 0.265 && !opposite))) {
                int checkY = getCheckY(y);
                if (checkY > -1){
                    if (level.isClientSide()) return InteractionResult.CONSUME;
                    else {
                        BlockEntity blockEntity = level.getBlockEntity(pos);
                        if (blockEntity instanceof ClipboardBlockEntity clipboard) {
                            List<Page> pages = getPages(clipboard, checkY);
                            clipboard.setContent(clipboard.getContent().setPages(pages));
                            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    private int getCheckY(double y) {
        int checkY = -1;
        if (y < 0.22 && y > 0.18) checkY = 8;
        if (y < 0.28 && y > 0.24) checkY = 7;
        if (y < 0.34 && y > 0.3) checkY = 6;
        if (y < 0.4 && y > 0.36) checkY = 5;
        if (y < 0.46 && y > 0.42) checkY = 4;
        if (y < 0.52 && y > 0.48) checkY = 3;
        if (y < 0.58 && y > 0.54) checkY = 2;
        if (y < 0.64 && y > 0.6) checkY = 1;
        if (y < 0.7 && y > 0.66) checkY = 0;
        return checkY;
    }

    private List<Page> getPages(ClipboardBlockEntity clipboard, int checkY) {
        Page page = clipboard.getContent().pages().get(clipboard.getContent().active());
        CheckboxState checkboxState = page.checkboxes().get(checkY);
        checkboxState = checkboxState.cycle();
        List<CheckboxState> checkboxStates = new ArrayList<>(page.checkboxes());
        checkboxStates.set(checkY, checkboxState);
        page = page.setCheckboxes(checkboxStates);
        List<Page> pages = new ArrayList<>(clipboard.getContent().pages());
        pages.set(clipboard.getContent().active(), page);
        return pages;
    }
}
