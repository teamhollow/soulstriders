package net.teamhollow.soulstriders.entity.wisp;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.ai.goal.FollowTargetGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.init.SSEntities;

public class WispEntity extends PassiveEntity {
    public static final String id = "wisp";
    public static final EntityType.Builder<WispEntity> builder = EntityType.Builder
        .create(WispEntity::new, SpawnGroup.AMBIENT)
        .setDimensions(0.5F, 0.5F)
        .maxTrackingRange(5);
    public static final int[] spawnEggColors = { 10236982, 5065037 };

    public WispEntity(EntityType<? extends WispEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(8, new LookAtEntityGoal(this, SoulStriderEntity.class, 16.0F));
        this.targetSelector.add(2, new FollowTargetGoal<SoulStriderEntity>(this, SoulStriderEntity.class, true));
    }

    @Override
    protected void mobTick() {
        if (this.age >= 6000) {
            SoulStriderEntity soulStrider = SSEntities.SOUL_STRIDER.create(this.world);
            if (soulStrider != null) {
                soulStrider.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
                soulStrider.setBaby(true);
                soulStrider.setPersistent();
                this.world.spawnEntity(soulStrider);
            }
            this.remove();
            return;
        }

        LivingEntity target = this.getTarget();
        BlockPos randomPos = target == null ? new BlockPos(this.getX(), this.getY(), this.getZ()) : new BlockPos(target.getX() - this.getX(), target.getY() - this.getY(), target.getZ() - this.getZ());
        randomPos = new BlockPos(randomPos.getX() + this.random.nextInt(7) - this.random.nextInt(7),
                randomPos.getY() + this.random.nextInt(6) - 2.0D,
                randomPos.getZ() + this.random.nextInt(7) - this.random.nextInt(7));

        double x = randomPos.getX() + 0.5D - this.getX();
        double y = randomPos.getY() + 0.1D - this.getY();
        double z = randomPos.getZ() + 0.5D - this.getZ();

        Vec3d vec3d = this.getVelocity();
        vec3d = vec3d.add(
            (Math.signum(x) * 0.5D - vec3d.x) * 0.10000000149011612D,
            ((Math.signum(y) * 0.699999988079071D - vec3d.y) * 0.10000000149011612D) + (target != null && target.getEyeY() > this.getY() ? 0.177D : 0.061D),
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

    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }
}
