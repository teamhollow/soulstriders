package net.teamhollow.soulstriders.entity.wisp;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.init.SSEntities;

import javax.annotation.Nullable;

public class WispEntity extends AgeableEntity {
    public static final String id = "wisp";

    public WispEntity(EntityType<? extends WispEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(8, new LookAtGoal(this, SoulStriderEntity.class, 16.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, SoulStriderEntity.class, true));
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        tag.putInt("Age", this.ticksExisted);
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        this.ticksExisted = tag.getInt("Age");
    }

    @Override
    protected void updateAITasks() {
        if (this.ticksExisted >= 6000) {
            SoulStriderEntity soulStrider = SSEntities.SOUL_STRIDER.create(this.world);
            if (soulStrider != null) {
                soulStrider.setPositionAndRotation(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch);
                soulStrider.onInitialSpawn((ServerWorld) this.world, this.world.getDifficultyForLocation(soulStrider.getPosition()), SpawnReason.BREEDING, null, null);
                soulStrider.setNoAI(this.isAIDisabled());
                if (this.hasCustomName()) {
                    soulStrider.setCustomName(this.getCustomName());
                    soulStrider.setCustomNameVisible(this.isCustomNameVisible());
                }
                soulStrider.setChild(true);
                soulStrider.enablePersistence();
                this.world.addEntity(soulStrider);
            }
            this.remove();
            return;
        }

        LivingEntity target = getAttackTarget();
        BlockPos randomPos = target == null
                             ? new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ())
                             : new BlockPos(
                                 target.getPosX() - this.getPosX(),
                                 target.getPosY() - this.getPosY(),
                                 target.getPosZ() - this.getPosZ()
                             );
        randomPos = new BlockPos(
            randomPos.getX() + this.rand.nextInt(7) - this.rand.nextInt(7),
            randomPos.getY() + this.rand.nextInt(6) - 2.0D,
            randomPos.getZ() + this.rand.nextInt(7) - this.rand.nextInt(7)
        );

        double x = randomPos.getX() + 0.5D - this.getPosX();
        double y = randomPos.getY() + 0.1D - this.getPosY();
        double z = randomPos.getZ() + 0.5D - this.getPosZ();

        Vector3d vec3d = this.getMotion();
        vec3d = vec3d.add(
            (Math.signum(x) * 0.5D - vec3d.x) * 0.1D,
            (Math.signum(y) * 0.7D - vec3d.y) * 0.1D + (target != null && target.getEyeHeight() > this.getPosY() ? .177d : .061d),
            (Math.signum(z) * 0.5D - vec3d.z) * 0.1D
        );

        this.setMotion(vec3d);
        this.moveForward = 0.5F;

        this.rotationYaw += MathHelper.wrapDegrees((float) (MathHelper.atan2(vec3d.z, vec3d.x) * (180 / Math.PI)) - 90f - this.rotationYaw);
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
        return this.rand.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
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
        return dimensions.height / 2.0F;
    }

    @Nullable
    @Override
    public AgeableEntity func_241840_a(ServerWorld world, AgeableEntity parent) {
        return null;
    }
}
