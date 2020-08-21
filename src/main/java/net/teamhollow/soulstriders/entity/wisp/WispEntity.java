package net.teamhollow.soulstriders.entity.wisp;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;

public class WispEntity extends AmbientEntity {
    public static final String id = "wisp";
    public static final EntityType.Builder<WispEntity> builder = EntityType.Builder
        .create(WispEntity::new, SpawnGroup.AMBIENT)
        .setDimensions(0.5F, 0.9F)
        .maxTrackingRange(5);
    public static final int[] spawnEggColors = { 10236982, 5065037 };

    public WispEntity(EntityType<? extends WispEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new FollowTargetGoal<SoulStriderEntity>(this, SoulStriderEntity.class, 10, true, false, (livingEntity) -> {
            return Math.abs(livingEntity.getY() - this.getY()) <= 4.0D;
        }));
    }

    @Override
    protected void mobTick() {
        BlockPos randomPos = new BlockPos (
            this.getX() + this.random.nextInt(7) - this.random.nextInt(7),
            this.getY() + this.random.nextInt(6) - 2.0D,
            this.getZ() + this.random.nextInt(7) - this.random.nextInt(7)
        );

        double x = randomPos.getX() + 0.5D - this.getX();
        double y = randomPos.getY() + 0.1D - this.getY();
        double z = randomPos.getZ() + 0.5D - this.getZ();

        Vec3d vec3d = this.getVelocity();
        vec3d = vec3d.add(
            (Math.signum(x) * 0.5D - vec3d.x) * 0.10000000149011612D,
            ((Math.signum(y) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D) + 0.061D,
            (Math.signum(z) * 0.5D - vec3d.z) * 0.10000000149011612D
        );

        this.setVelocity(vec3d);
        this.forwardSpeed = 0.5F;

        this.yaw += MathHelper.wrapDegrees(((float) (MathHelper.atan2(vec3d.z, vec3d.x) * 57.2957763671875D) - 90.0F) - this.yaw);
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() * 0.95F;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return this.random.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BAT_DEATH;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {}

    @Override
    protected void tickCramming() {}

    public static DefaultAttributeContainer.Builder createWispAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0D);
    }

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
        return dimensions.height / 2.0F;
    }
}
