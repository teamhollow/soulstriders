package net.teamhollow.soulstriders.block;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.state.property.SSProperties;

import java.util.Objects;
import java.util.Random;

@SuppressWarnings("deprecation")
public class SoulStriderBulbBlock extends Block implements IWaterLoggable {
    public static final String id = "soul_strider_bulb";

    public static final IntegerProperty BULBS = SSProperties.BULBS;
    public static final IntegerProperty HATCH = BlockStateProperties.HATCH_0_2;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape ONE_BULB_SHAPE = makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 4.0D, 13.0D);
    protected static final VoxelShape TWO_BULBS_SHAPE = makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape THREE_BULBS_SHAPE = makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
    protected static final VoxelShape FOUR_BULBS_SHAPE = makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);

    public SoulStriderBulbBlock(AbstractBlock.Properties settings) {
        super(settings);
        this.setDefaultState(getStateContainer().getBaseState().with(BULBS, 1).with(HATCH, 0));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.func_242406_i(pos).filter(Biomes.SOUL_SAND_VALLEY::equals).isPresent() && world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS)) {
            int hatch = state.get(HATCH);
            if (hatch < 2) {
                world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
                world.setBlockState(pos, state.with(HATCH, hatch + 1), 2);
            } else {
                world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
                world.removeBlock(pos, false);

                for (int i = 0; i < state.get(BULBS); ++i) {
                    world.playEvent(2001, pos, Block.getStateId(state));
                    WispEntity wispEntity = SSEntities.WISP.create(world);
                    assert wispEntity != null;
                    wispEntity.setPositionAndRotation(pos.getX() + 0.3D + i * 0.2D, pos.getY(), pos.getZ() + 0.3D, 0.0F, 0.0F);
                    world.addEntity(wispEntity);
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState blockState = context.getWorld().getBlockState(context.getPos());
        if (blockState.isIn(this)) {
            return blockState.with(BULBS, Math.min(4, blockState.get(BULBS) + 1));
        } else {
            FluidState fluidState = context.getWorld().getFluidState(context.getPos());
            return Objects.requireNonNull(super.getStateForPlacement(context)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
        pos = pos.down();
        return !world.getBlockState(pos).getCollisionShape(world, pos).project(Direction.UP).isEmpty();
    }

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction direction, BlockState newState,
                                          IWorld world, BlockPos pos, BlockPos posFrom) {
        if (!state.isValidPosition(world, pos)) {
            return Blocks.AIR.getDefaultState();
        } else {
            if (state.get(WATERLOGGED)) {
                world.getPendingFluidTicks().scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
            }

            return super.updatePostPlacement(state, direction, newState, world, pos, posFrom);
        }
    }

    @Override
    public boolean isReplaceable(BlockState state, BlockItemUseContext context) {
        return context.getItem().getItem() == asItem() && state.get(BULBS) < 4 || super.isReplaceable(state, context);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
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
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BULBS, HATCH, WATERLOGGED);
    }

    @Override
    public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
}
