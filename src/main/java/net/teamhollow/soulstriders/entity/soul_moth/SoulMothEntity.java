package net.teamhollow.soulstriders.entity.soul_moth;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.init.SSBlocks;
import net.teamhollow.soulstriders.init.SSItems;

public class SoulMothEntity extends CreatureEntity {
    public static final String id = "soul_moth";

    private BlockPos targetPos;

    public SoulMothEntity(EntityType<? extends SoulMothEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(2, new LookAtGoal(this, WispEntity.class, 16.0F));
        this.goalSelector.addGoal(1, new TargetSoulifiableBlock(1.5D, 14, 8));
        this.goalSelector.addGoal(1, new SoulifyBlockTask(1.5D, 2, 2));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, WispEntity.class, true));
//        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, SoulStriderEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isRemote)
            this.world.addParticle(ParticleTypes.MYCELIUM, this.getPosX(), this.getPosY(), this.getPosZ(), 0, 0, 0);
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (!this.isAlive())
            return;

        if (this.targetPos != null) {
            BlockState targetPosBlockState = world.getBlockState(targetPos);
            if (!(targetPosBlockState.isIn(SSBlocks.SOUL_STRIDER_BULB)
                      || targetPosBlockState.isIn(Blocks.TORCH)
                      || targetPosBlockState.isIn(Blocks.WALL_TORCH)
                      || targetPosBlockState.isIn(Blocks.LANTERN)
                      || targetPosBlockState.isIn(Blocks.CAMPFIRE) && targetPosBlockState.get(CampfireBlock.LIT))) {
                this.targetPos = null;
            }
        }

        LivingEntity targetEntity = this.getAttackingEntity();
//        if (targetEntity instanceof SoulStriderEntity && !((SoulStriderEntity) targetEntity).isHiding())
//            targetEntity = null;
        BlockPos randomPos = targetEntity == null ? this.targetPos : targetEntity.getPosition();
        randomPos = randomPos == null
                    ? new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ())
                    : new BlockPos(
                        randomPos.getX(),
                        randomPos.getY(),
                        randomPos.getZ()
                    )
        ;
        randomPos = new BlockPos(
            randomPos.getX() + this.rand.nextInt(7) - this.rand.nextInt(7),
            randomPos.getY() + this.rand.nextInt(6) - 2,
            randomPos.getZ() + this.rand.nextInt(7) - this.rand.nextInt(7)
        );

        double x = randomPos.getX() + 0.5 - this.getPosX();
        double y = randomPos.getY() + 0.1 - this.getPosY();
        double z = randomPos.getZ() + 0.5 - this.getPosZ();

        Vector3d vec3d = this.getMotion();
        vec3d = vec3d.add(
            (Math.signum(x) * 0.5 - vec3d.x) * 0.1,
            (Math.signum(y) * 0.7 - vec3d.y) * 0.1 + (targetEntity != null && targetEntity.getEyeHeight() > getPosY() ? 0.177D : 0.061D),
            (Math.signum(z) * 0.5 - vec3d.z) * 0.1
        );

        this.setMotion(vec3d);
        this.moveForward = 0.5F;

        this.rotationYaw += MathHelper.wrapDegrees((float) (MathHelper.atan2(vec3d.z, vec3d.x) * (180 / Math.PI)) - 90 - rotationYaw);
    }


    @Override
    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getHeldItem(hand);
        if (itemStack.getItem() == Items.GLASS_BOTTLE) {
            player.playSound(SoundEvents.BLOCK_BEEHIVE_ENTER, 1.0F, 1.0F);
            ItemStack itemStack2 = DrinkHelper.fill(itemStack, player, SSItems.SOUL_MOTH_IN_A_BOTTLE.getDefaultInstance());
            player.setHeldItem(hand, itemStack2);

            this.remove();
            return ActionResultType.func_233537_a_(this.world.isRemote);
        } else {
            return super.func_230254_b_(player, hand);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0F;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entity) {
    }

    @Override
    protected void collideWithNearbyEntities() {
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean onLivingFall(float fallDistance, float damageMultiplier) {
        return false;
    }

    @Override
    protected void updateFallState(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
    }

    @Override
    public boolean doesEntityNotTriggerPressurePlate() {
        return true;
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntitySize dimensions) {
        return dimensions.height;
    }

    class SoulifyBlockTask extends MoveToBlockGoal {
        protected int timer;
        protected int range;

        SoulifyBlockTask(double speed, int range, int maxYDifference) {
            super(SoulMothEntity.this, speed, range, maxYDifference);
            this.range = range;
        }

        @Override
        public double getTargetDistanceSq() {
            return 1.5D;
        }

        @Override
        public boolean shouldMove() {
            return this.timeoutCounter % 100 == 0;
        }

        @Override
        public void tick() {
            if (this.getIsAboveDestination()) {
                if (this.timer >= 40) {
                    this.soulifyBlock();
                } else {
                    this.timer++;
                }
            }

            super.tick();
        }

        @Override
        protected boolean shouldMoveTo(IWorldReader world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isIn(SSBlocks.SOUL_STRIDER_BULB)
                       || blockState.isIn(Blocks.TORCH)
                       || blockState.isIn(Blocks.WALL_TORCH)
                       || blockState.isIn(Blocks.LANTERN)
                       || blockState.isIn(Blocks.CAMPFIRE) && blockState.get(CampfireBlock.LIT);
        }

        protected void soulifyBlock() {
            if (world.getGameRules().getBoolean(GameRules.MOB_GRIEFING)) {
                BlockPos pos = targetPos;
                BlockState blockState = world.getBlockState(pos);
                BlockState newState = blockState;

                switch (Registry.BLOCK.getKey(blockState.getBlock()).toString()) {
                    case "minecraft:torch":
                        newState = Blocks.SOUL_TORCH.getDefaultState();
                        break;
                    case "minecraft:wall_torch":
                        newState = Blocks.SOUL_WALL_TORCH.getDefaultState()
                                                         .with(WallTorchBlock.HORIZONTAL_FACING, blockState.get(WallTorchBlock.HORIZONTAL_FACING));
                        break;
                    case "minecraft:campfire":
                        newState = Blocks.SOUL_CAMPFIRE.getDefaultState()
                                                       .with(CampfireBlock.FACING, blockState.get(CampfireBlock.FACING))
                                                       .with(CampfireBlock.LIT, blockState.get(CampfireBlock.LIT))
                                                       .with(CampfireBlock.SIGNAL_FIRE, blockState.get(CampfireBlock.SIGNAL_FIRE))
                                                       .with(CampfireBlock.WATERLOGGED, blockState.get(CampfireBlock.WATERLOGGED));
                        break;
                    case "minecraft:lantern":
                        newState = Blocks.SOUL_LANTERN.getDefaultState()
                                                      .with(LanternBlock.HANGING, blockState.get(LanternBlock.HANGING));
                        break;
                }

                world.setBlockState(pos, newState);
            }
        }

        @Override
        public void startExecuting() {
            this.timer = 0;
            super.startExecuting();
        }

        @Override
        public void resetTask() {
            SoulMothEntity entity = (SoulMothEntity) creature;
            entity.targetPos = null;

            super.resetTask();
        }

        @Override
        public boolean shouldExecute() {
            double localEntityRange = 2.0D;

            return super.shouldExecute() && creature.world.getEntitiesWithinAABBExcludingEntity(
                creature,
                new AxisAlignedBB(
                    creature.getPosX() - localEntityRange, creature.getPosY() - localEntityRange, creature.getPosZ() - localEntityRange,
                    creature.getPosX() + localEntityRange, creature.getPosY() + localEntityRange, creature.getPosZ() + localEntityRange
                )
            ).size() >= 14;
        }
    }

    class TargetSoulifiableBlock extends MoveToBlockGoal {
        protected int timer;

        TargetSoulifiableBlock(double speed, int range, int maxYDifference) {
            super(SoulMothEntity.this, speed, range, maxYDifference);
        }

        @Override
        public boolean shouldContinueExecuting() {
            return false;
        }

        @Override
        protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isIn(SSBlocks.SOUL_STRIDER_BULB)
                       || blockState.isIn(Blocks.TORCH)
                       || blockState.isIn(Blocks.WALL_TORCH)
                       || blockState.isIn(Blocks.LANTERN)
                       || blockState.isIn(Blocks.CAMPFIRE) && blockState.get(CampfireBlock.LIT);
        }

        @Override
        public void startExecuting() {
            this.timer = 0;

            SoulMothEntity entity = (SoulMothEntity) creature;
            entity.targetPos = this.destinationBlock;

            super.startExecuting();
        }
    }
}
