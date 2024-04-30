package io.github.diiiaz.super_pistons.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.diiiaz.super_pistons.ModSounds;
import io.github.diiiaz.super_pistons.block.ModBlocks;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.*;

public class SuperPistonBlock extends FacingBlock {
    public static final BooleanProperty EXTENDED = Properties.EXTENDED;
//    public static final int field_31373 = 0;
//    public static final int field_31374 = 1;
//    public static final int field_31375 = 2;
//    public static final float field_31376 = 4.0f;
    protected static final VoxelShape EXTENDED_EAST_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_WEST_SHAPE = Block.createCuboidShape(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_SOUTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
    protected static final VoxelShape EXTENDED_NORTH_SHAPE = Block.createCuboidShape(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape EXTENDED_UP_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
    protected static final VoxelShape EXTENDED_DOWN_SHAPE = Block.createCuboidShape(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
    private final boolean sticky;

    public SuperPistonBlock(boolean sticky, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(EXTENDED, false));
        this.sticky = sticky;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(EXTENDED).booleanValue()) {
            switch (state.get(FACING)) {
                case DOWN: {
                    return EXTENDED_DOWN_SHAPE;
                }
                default: {
                    return EXTENDED_UP_SHAPE;
                }
                case NORTH: {
                    return EXTENDED_NORTH_SHAPE;
                }
                case SOUTH: {
                    return EXTENDED_SOUTH_SHAPE;
                }
                case WEST: {
                    return EXTENDED_WEST_SHAPE;
                }
                case EAST:
            }
            return EXTENDED_EAST_SHAPE;
        }
        return VoxelShapes.fullCube();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (!world.isClient && world.getBlockEntity(pos) == null) {
            this.tryMove(world, pos, state);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite())).with(EXTENDED, false);
    }

    private void tryMove(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(FACING);
        boolean bl = this.shouldExtend(world, pos, direction);
        if (bl && !state.get(EXTENDED).booleanValue()) {
            if (new SuperPistonHandler(world, pos, direction, true).calculatePush()) {
                world.addSyncedBlockEvent(pos, this, 0, direction.getId());
            }
        } else if (!bl && state.get(EXTENDED).booleanValue()) {
            SuperPistonBlockEntity superPistonBlockEntity;
            BlockEntity blockEntity;
            BlockPos blockPos = pos.offset(direction, 2);
            BlockState blockState = world.getBlockState(blockPos);
            int i = 1;
            if (blockState.isOf(ModBlocks.MOVING_SUPER_PISTON) && blockState.get(FACING) == direction && (blockEntity = world.getBlockEntity(blockPos)) instanceof SuperPistonBlockEntity && (superPistonBlockEntity = (SuperPistonBlockEntity)blockEntity).isExtending() && (superPistonBlockEntity.getProgress(0.0f) < 0.5f || world.getTime() == superPistonBlockEntity.getSavedWorldTime() || ((ServerWorld)world).isInBlockTick())) {
                i = 2;
            }
            world.addSyncedBlockEvent(pos, this, i, direction.getId());
        }
    }

    private boolean shouldExtend(World world, BlockPos pos, Direction pistonFace) {
        for (Direction direction : Direction.values()) {
            if (direction == pistonFace || !world.isEmittingRedstonePower(pos.offset(direction), direction)) continue;
            return true;
        }
        if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos = pos.up();
        for (Direction direction2 : Direction.values()) {
            if (direction2 == Direction.DOWN || !world.isEmittingRedstonePower(blockPos.offset(direction2), direction2)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        Direction direction = state.get(FACING);
        if (!world.isClient) {
            boolean bl = this.shouldExtend(world, pos, direction);
            if (bl && (type == 1 || type == 2)) {
                world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), Block.NOTIFY_LISTENERS);
                return false;
            }
            if (!bl && type == 0) {
                return false;
            }
        }
        if (type == 0) {
            if (!this.move(world, pos, direction, true)) return false;
            world.setBlockState(pos, (BlockState)state.with(EXTENDED, true), Block.NOTIFY_ALL | Block.MOVED);
            world.playSound(null, pos, ModSounds.BLOCK_SUPER_PISTON_MOVE, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.25f + 0.9f);
            world.emitGameEvent(null, GameEvent.BLOCK_ACTIVATE, pos);
            return true;
        } else {
            if (type != 1 && type != 2) return true;
            BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction));
            if (blockEntity instanceof SuperPistonBlockEntity) {
                ((SuperPistonBlockEntity)blockEntity).finish();
            }
            BlockState blockState = (BlockState)((BlockState)ModBlocks.MOVING_SUPER_PISTON.getDefaultState().with(SuperPistonExtensionBlock.FACING, direction)).with(SuperPistonExtensionBlock.TYPE, this.sticky ? SuperPistonType.STICKY : SuperPistonType.DEFAULT);
            world.setBlockState(pos, blockState, Block.NO_REDRAW | Block.FORCE_STATE);
            world.addBlockEntity(SuperPistonExtensionBlock.createBlockEntityPiston(pos, blockState, (BlockState)this.getDefaultState().with(FACING, Direction.byId(data & 7)), direction, false, true));
            world.updateNeighbors(pos, blockState.getBlock());
            blockState.updateNeighbors(world, pos, Block.NOTIFY_LISTENERS);
            if (this.sticky) {
                SuperPistonBlockEntity superPistonBlockEntity;
                BlockEntity blockEntity2;
                BlockPos blockPos = pos.add(direction.getOffsetX() * 2, direction.getOffsetY() * 2, direction.getOffsetZ() * 2);
                BlockState blockState2 = world.getBlockState(blockPos);
                boolean bl2 = false;
                if (blockState2.isOf(ModBlocks.MOVING_SUPER_PISTON) && (blockEntity2 = world.getBlockEntity(blockPos)) instanceof SuperPistonBlockEntity && (superPistonBlockEntity = (SuperPistonBlockEntity)blockEntity2).getFacing() == direction && superPistonBlockEntity.isExtending()) {
                    superPistonBlockEntity.finish();
                    bl2 = true;
                }
                if (!bl2) {
                    if (type == 1 && !blockState2.isAir() && SuperPistonBlock.isMovable(blockState2, world, blockPos, direction.getOpposite(), false, direction) && (blockState2.getPistonBehavior() == PistonBehavior.NORMAL || blockState2.isOf(Blocks.PISTON) || blockState2.isOf(Blocks.STICKY_PISTON) || blockState2.isOf(ModBlocks.SUPER_PISTON) || blockState2.isOf(ModBlocks.SUPER_STICKY_PISTON))) {
                        this.move(world, pos, direction, false);
                    } else {
                        world.removeBlock(pos.offset(direction), false);
                    }
                }
            } else {
                world.removeBlock(pos.offset(direction), false);
            }
            world.playSound(null, pos, ModSounds.BLOCK_SUPER_PISTON_MOVE, SoundCategory.BLOCKS, 1f, world.random.nextFloat() * 0.15f + 0.7f);
            world.emitGameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
        }
        return true;
    }

    public static boolean isMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir) {
        if (pos.getY() < world.getBottomY() || pos.getY() > world.getTopY() - 1 || !world.getWorldBorder().contains(pos)) {
            return false;
        }
        if (state.isAir()) {
            return true;
        }
        if (state.isOf(Blocks.OBSIDIAN) || state.isOf(Blocks.CRYING_OBSIDIAN) || state.isOf(Blocks.RESPAWN_ANCHOR) || state.isOf(Blocks.REINFORCED_DEEPSLATE) || state.isOf(Blocks.END_PORTAL_FRAME)) {
            return false;
        }
        if (direction == Direction.DOWN && pos.getY() == world.getBottomY()) {
            return false;
        }
        if (direction == Direction.UP && pos.getY() == world.getTopY() - 1) {
            return false;
        }
        if (state.getBlock() instanceof PistonBlock || state.getBlock() instanceof SuperPistonBlock) {
            if (state.get(EXTENDED)) {
                return false;
            }
        } else {
            if (state.getHardness(world, pos) == -1.0f) {
                return false;
            }
            switch (state.getPistonBehavior()) {
                case BLOCK -> {
                    return false;
                }
                case DESTROY -> {
                    return canBreak;
                }
                case PUSH_ONLY -> {
                    return direction == pistonDir;
                }
            }
        }
        if (state.hasBlockEntity()) {
            return isBlockEntityMoveable(state.getBlock());
        }
        return !state.hasBlockEntity();
    }


    private static boolean isBlockEntityMoveable(Block block)
    {
        return  block != Blocks.END_GATEWAY &&
                block != Blocks.END_PORTAL &&
                block != Blocks.MOVING_PISTON &&
                block != ModBlocks.MOVING_SUPER_PISTON;
    }


    ThreadLocal<List<BlockEntity>> pushedBlockEntities = new ThreadLocal<>();

    private boolean move(World world, BlockPos pos, Direction dir, boolean extend) {
        int l;
        BlockPos blockPos5;
        BlockPos blockPos3;
        int k;
        SuperPistonHandler superPistonHandler;
        BlockPos blockPos = pos.offset(dir);
        if (!extend && world.getBlockState(blockPos).isOf(ModBlocks.SUPER_PISTON_HEAD)) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
        }
        if (!(superPistonHandler = new SuperPistonHandler(world, pos, dir, extend)).calculatePush()) {
            return false;
        }
        HashMap<BlockPos, BlockState> map = Maps.newHashMap();
        List<BlockPos> list = superPistonHandler.getMovedBlocks();
        ArrayList<BlockState> list2 = Lists.newArrayList();

        for (BlockPos blockPos2 : list) {
            BlockState blockState = world.getBlockState(blockPos2);
            list2.add(blockState);
            map.put(blockPos2, blockState);
        }

        // some of the code here is copied and modified from the carpet mod : https://modrinth.com/mod/carpet
        pushedBlockEntities.set(Lists.newArrayList());
        for (int i = 0; i < list.size(); i++) {
            BlockPos blockEntityPos = list.get(i);
            BlockEntity blockEntity = (list2.get(i).hasBlockEntity()) ? world.getBlockEntity(blockEntityPos) : null;
            pushedBlockEntities.get().add(blockEntity);
            if (blockEntity != null) {
                world.removeBlockEntity(blockEntityPos);
            }
        }

        List<BlockPos> list3 = superPistonHandler.getBrokenBlocks();
        BlockState[] blockStates = new BlockState[list.size() + list3.size()];
        Direction direction = extend ? dir : dir.getOpposite();
        int j = 0;


        for (k = list3.size() - 1; k >= 0; --k) {
            blockPos3 = list3.get(k);
            BlockState blockState = world.getBlockState(blockPos3);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity(blockPos3) : null;
            SuperPistonBlock.dropStacks(blockState, world, blockPos3, blockEntity);
            world.setBlockState(blockPos3, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            world.emitGameEvent(GameEvent.BLOCK_DESTROY, blockPos3, GameEvent.Emitter.of(blockState));
            if (!blockState.isIn(BlockTags.FIRE)) {
                world.addBlockBreakParticles(blockPos3, blockState);
            }
            blockStates[j++] = blockState;
        }
        for (k = list.size() - 1; k >= 0; --k) {
            blockPos3 = list.get(k);
            BlockState blockState = world.getBlockState(blockPos3);
            blockPos3 = blockPos3.offset(direction);
            map.remove(blockPos3);
            BlockState blockState3 = (BlockState)ModBlocks.MOVING_SUPER_PISTON.getDefaultState().with(FACING, dir);
            world.setBlockState(blockPos3, blockState3, Block.NO_REDRAW | Block.MOVED);

            // NEW
            BlockEntity blockEntityPiston = SuperPistonExtensionBlock.createBlockEntityPiston(blockPos3, blockState3, (BlockState)list2.get(k), dir, extend, false);
            ((SuperPistonBlockEntity)blockEntityPiston).setPushedBlockEntity(pushedBlockEntities.get().get(k));
            world.addBlockEntity(blockEntityPiston);

            // END NEW
            blockStates[j++] = blockState;
        }
        if (extend) {
            SuperPistonType superPistonType = this.sticky ? SuperPistonType.STICKY : SuperPistonType.DEFAULT;
            BlockState blockState4 = (BlockState)((BlockState)ModBlocks.SUPER_PISTON_HEAD.getDefaultState().with(SuperPistonHeadBlock.FACING, dir)).with(SuperPistonHeadBlock.TYPE, superPistonType);
            BlockState blockState = (BlockState)((BlockState)ModBlocks.MOVING_SUPER_PISTON.getDefaultState().with(SuperPistonExtensionBlock.FACING, dir)).with(SuperPistonExtensionBlock.TYPE, this.sticky ? SuperPistonType.STICKY : SuperPistonType.DEFAULT);
            map.remove(blockPos);
            world.setBlockState(blockPos, blockState, Block.NO_REDRAW | Block.MOVED);
            world.addBlockEntity(SuperPistonExtensionBlock.createBlockEntityPiston(blockPos, blockState, blockState4, dir, true, true));
        }
        BlockState blockState5 = Blocks.AIR.getDefaultState();
        for (BlockPos blockPos2 : map.keySet()) {
            world.setBlockState(blockPos2, blockState5, Block.NOTIFY_LISTENERS | Block.FORCE_STATE | Block.MOVED);
        }
        for (Map.Entry entry : map.entrySet()) {
            blockPos5 = (BlockPos)entry.getKey();
            BlockState blockState6 = (BlockState)entry.getValue();
            blockState6.prepare(world, blockPos5, 2);
            blockState5.updateNeighbors(world, blockPos5, Block.NOTIFY_LISTENERS);
            blockState5.prepare(world, blockPos5, 2);
        }
        j = 0;
        for (l = list3.size() - 1; l >= 0; --l) {
            BlockState blockState = blockStates[j++];
            blockPos5 = list3.get(l);
            blockState.prepare(world, blockPos5, 2);
            world.updateNeighborsAlways(blockPos5, blockState.getBlock());
        }
        for (l = list.size() - 1; l >= 0; --l) {
            world.updateNeighborsAlways(list.get(l), blockStates[j++].getBlock());
        }
        if (extend) {
            world.updateNeighborsAlways(blockPos, ModBlocks.SUPER_PISTON_HEAD);
        }
        return true;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return state.get(EXTENDED);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

}
