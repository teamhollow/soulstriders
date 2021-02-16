package net.teamhollow.soulstriders.entity.soul_moth;

import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.init.SSBlocks;
import net.teamhollow.soulstriders.init.SSItems;

public class SoulMothEntity extends PathAwareEntity {
    public static final String id = "soul_moth";

    private BlockPos targetPos;

    public SoulMothEntity(EntityType<? extends SoulMothEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(2, new LookAtEntityGoal(this, WispEntity.class, 16.0F));
        this.goalSelector.add(1, new SoulMothEntity.TargetSoulifiableBlock(1.5D, 14, 8));
        this.goalSelector.add(1, new SoulMothEntity.SoulifyBlockGoal(1.5D, 2, 2));
        this.targetSelector.add(2, new FollowTargetGoal<>(this, WispEntity.class, true));
        this.targetSelector.add(2, new FollowTargetGoal<>(this, SoulStriderEntity.class, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.world.isClient())
            this.world.addParticle(ParticleTypes.MYCELIUM, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    @Override
    protected void mobTick() {
        super.mobTick();

        if (!this.isAlive())
            return;

        if (this.targetPos != null) {
            BlockState targetPosBlockState = world.getBlockState(targetPos);
            if (!( targetPosBlockState.isOf(SSBlocks.SOUL_STRIDER_BULB)
                || targetPosBlockState.isOf(Blocks.TORCH)
                || targetPosBlockState.isOf(Blocks.WALL_TORCH)
                || targetPosBlockState.isOf(Blocks.LANTERN)
                || (targetPosBlockState.isOf(Blocks.CAMPFIRE) && targetPosBlockState.get(CampfireBlock.LIT))
                )
            ) this.targetPos = null;
        }

        LivingEntity targetEntity = this.getTarget();
        if (targetEntity instanceof SoulStriderEntity && !((SoulStriderEntity) targetEntity).isHiding())
            targetEntity = null;
        BlockPos randomPos = targetEntity == null ? this.targetPos : targetEntity.getBlockPos();
        randomPos = randomPos == null
            ? new BlockPos(this.getX(), this.getY(), this.getZ())
            : new BlockPos(
                randomPos.getX(),
                randomPos.getY(),
                randomPos.getZ()
            )
        ;
        randomPos = new BlockPos(
            randomPos.getX() + this.random.nextInt(7) - this.random.nextInt(7),
            randomPos.getY() + this.random.nextInt(6) - 2.0D,
            randomPos.getZ() + this.random.nextInt(7) - this.random.nextInt(7)
        );

        double x = randomPos.getX() + 0.5D - this.getX();
        double y = randomPos.getY() + 0.1D - this.getY();
        double z = randomPos.getZ() + 0.5D - this.getZ();

        Vec3d vec3d = this.getVelocity();
        vec3d = vec3d.add(
            (Math.signum(x) * 0.5D - vec3d.x) * 0.10000000149011612D,
            ((Math.signum(y) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D) + (targetEntity != null && targetEntity.getEyeY() > this.getY() ? 0.177D : 0.061D),
            (Math.signum(z) * 0.5D - vec3d.z) * 0.10000000149011612D
        );

        this.setVelocity(vec3d);
        this.forwardSpeed = 0.5F;

        this.yaw += MathHelper.wrapDegrees(((float) (MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875D) - 90.0F) - this.yaw);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == Items.GLASS_BOTTLE) {
            player.playSound(SoundEvents.BLOCK_BEEHIVE_ENTER, 1.0F, 1.0F);
            ItemStack itemStack2 = ItemUsage.method_30012(itemStack, player, SSItems.SOUL_MOTH_IN_A_BOTTLE.getDefaultStack());
            player.setStackInHand(hand, itemStack2);

            this.remove();
            return ActionResult.success(this.world.isClient);
        } else {
            return super.interactMob(player, hand);
        }
    }

    @Override
    protected float getSoundVolume() {
        return 0F;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {}

    @Override
    protected void tickCramming() {}

    @Override
    protected boolean canClimb() {
        return false;
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
        return false;
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {}

    @Override
    public boolean canAvoidTraps() {
        return true;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height;
    }

    class SoulifyBlockGoal extends MoveToTargetPosGoal {
        protected int timer;
        protected int range;

        public SoulifyBlockGoal(double speed, int range, int maxYDifference) {
            super(SoulMothEntity.this, speed, range, maxYDifference);
            this.range = range;
        }

        @Override
        public double getDesiredSquaredDistanceToTarget() {
            return 1.5D;
        }

        @Override
        public boolean shouldResetPath() {
            return this.tryingTime % 100 == 0;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isOf(SSBlocks.SOUL_STRIDER_BULB)
                || blockState.isOf(Blocks.TORCH)
                || blockState.isOf(Blocks.WALL_TORCH)
                || blockState.isOf(Blocks.LANTERN)
                || (blockState.isOf(Blocks.CAMPFIRE) && blockState.get(CampfireBlock.LIT));
        }

        @Override
        public void tick() {
            if (this.hasReached()) {
                if (this.timer >= 40) {
                    this.soulifyBlock();
                } else {
                    this.timer++;
                }
            }

            super.tick();
        }

        protected void soulifyBlock() {
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
                BlockPos pos = targetPos;
                BlockState blockState = world.getBlockState(pos);
                BlockState newState = blockState;

                switch (Registry.BLOCK.getId(blockState.getBlock()).toString()) {
                    case "minecraft:torch":
                        newState = Blocks.SOUL_TORCH.getDefaultState();
                        break;
                    case "minecraft:wall_torch":
                        newState = Blocks.SOUL_WALL_TORCH.getDefaultState()
                                .with(WallTorchBlock.FACING, blockState.get(WallTorchBlock.FACING));
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
        public void start() {
            this.timer = 0;
            super.start();
        }
        @Override
        public void stop() {
            SoulMothEntity entity = (SoulMothEntity) mob;
            entity.targetPos = null;

            super.stop();
        }

        @Override
        public boolean canStart() {
            double localEntityRange = 2.0D;

            return super.canStart() && mob.world.getOtherEntities(
                mob,
                new Box(
                    mob.getX() - localEntityRange, mob.getY() - localEntityRange, mob.getZ() - localEntityRange,
                    mob.getX() + localEntityRange, mob.getY() + localEntityRange, mob.getZ() + localEntityRange)
                ).size() >= 14;
        }
    }

    class TargetSoulifiableBlock extends MoveToTargetPosGoal {
        protected int timer;

        public TargetSoulifiableBlock(double speed, int range, int maxYDifference) {
            super(SoulMothEntity.this, speed, range, maxYDifference);
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isOf(SSBlocks.SOUL_STRIDER_BULB)
                || blockState.isOf(Blocks.TORCH)
                || blockState.isOf(Blocks.WALL_TORCH)
                || blockState.isOf(Blocks.LANTERN)
                || (blockState.isOf(Blocks.CAMPFIRE) && blockState.get(CampfireBlock.LIT));
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            this.timer = 0;

            SoulMothEntity entity = (SoulMothEntity) mob;
            entity.targetPos = this.targetPos;

            super.start();
        }
    }
}
