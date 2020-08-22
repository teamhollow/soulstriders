package net.teamhollow.soulstriders.entity.soul_moth;

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
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;
import net.teamhollow.soulstriders.init.SSItems;

public class SoulMothEntity extends AmbientEntity {
    public static final String id = "soul_moth";
    public static final EntityType.Builder<SoulMothEntity> builder = EntityType.Builder
        .create(SoulMothEntity::new, SpawnGroup.AMBIENT)
        .setDimensions(0.07F, 0.07F)
        .maxTrackingRange(5);
    public static final int[] spawnEggColors = { 10236982, 5065037 };

    public SoulMothEntity(EntityType<? extends SoulMothEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();

        this.goalSelector.add(8, new LookAtEntityGoal(this, WispEntity.class, 16.0F));
        this.targetSelector.add(2, new FollowTargetGoal<WispEntity>(this, WispEntity.class, true));
    }

    @Override
    protected void mobTick() {
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
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.getItem() == Items.GLASS_BOTTLE) {
            player.playSound(SoundEvents.BLOCK_BEEHIVE_ENTER, 1.0F, 1.0F);
            ItemStack itemStack2 = ItemUsage.method_30012(itemStack, player, SSItems.SOUL_MOTH_IN_A_BOTTLE.getStackForRender());
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

    public static DefaultAttributeContainer.Builder createSoulMothAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D);
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
        return dimensions.height;
    }
}
