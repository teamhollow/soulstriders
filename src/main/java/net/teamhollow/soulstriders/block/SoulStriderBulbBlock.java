package net.teamhollow.soulstriders.block;

import net.minecraft.block.*;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.BiomeKeys;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.state.property.SSProperties;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("deprecation")
public class SoulStriderBulbBlock extends Block implements Waterloggable {
    public static final String id = "soul_strider_bulb";

    public static final IntProperty BULBS = SSProperties.BULBS;
    public static final IntProperty HATCH = Properties.HATCH;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    protected static final VoxelShape ONE_BULB_SHAPE = Block.createCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 4.0D, 13.0D);
    protected static final VoxelShape TWO_BULBS_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape THREE_BULBS_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape FOUR_BULBS_SHAPE = Block.createCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    public SoulStriderBulbBlock() {
        super(Settings.of(Material.STONE, MaterialColor.STONE).ticksRandomly().strength(0.5F).nonOpaque().sounds(BlockSoundGroup.STONE));
        this.setDefaultState(this.stateManager.getDefaultState().with(BULBS, 1).with(HATCH, 0));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (Objects.equals(world.method_31081(pos), Optional.of(BiomeKeys.SOUL_SAND_VALLEY)) && world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS)) {
            int hatch = state.get(HATCH);
            if (hatch < 2) {
                world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
                world.setBlockState(pos, state.with(HATCH, hatch + 1), 2);
            } else {
                world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
                world.removeBlock(pos, false);

                for (int i = 0; i < state.get(BULBS); ++i) {
                    world.syncWorldEvent(2001, pos, Block.getRawIdFromState(state));
                    WispEntity wispEntity = SSEntities.WISP.create(world);
                    assert wispEntity != null;
                    wispEntity.refreshPositionAndAngles(pos.getX() + 0.3D + i * 0.2D, pos.getY(), pos.getZ() + 0.3D, 0.0F, 0.0F);
                    world.spawnEntity(wispEntity);
                }
            }
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getBlockPos());
        if (blockState.isOf(this)) {
            return blockState.with(BULBS, Math.min(4, blockState.get(BULBS) + 1));
        } else {
            FluidState fluidState = context.getWorld().getFluidState(context.getBlockPos());
            return Objects.requireNonNull(super.getPlacementState(context)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        pos = pos.down();
        return !world.getBlockState(pos).getCollisionShape(world, pos).getFace(Direction.UP).isEmpty();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
            WorldAccess world, BlockPos pos, BlockPos posFrom) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            if (state.get(WATERLOGGED)) {
                world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            }

            return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return context.getStack().getItem() == this.asItem() && state.get(BULBS) < 4 || super.canReplace(state, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        switch (state.get(BULBS)) {
            case 1:
            default:
                return ONE_BULB_SHAPE;
            case 2:
                return TWO_BULBS_SHAPE;
            case 3:
                return THREE_BULBS_SHAPE;
            case 4:
                return FOUR_BULBS_SHAPE;
        }
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(BULBS, HATCH, WATERLOGGED);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
