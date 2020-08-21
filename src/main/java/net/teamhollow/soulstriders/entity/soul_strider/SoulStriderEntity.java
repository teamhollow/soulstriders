package net.teamhollow.soulstriders.entity.soul_strider;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biomes;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulStriderEntity extends AnimalEntity implements ItemSteerable, Saddleable {
    public static final String id = "soul_strider";
    public static final EntityType.Builder<SoulStriderEntity> builder = EntityType.Builder
        .create(SoulStriderEntity::new, SpawnGroup.CREATURE)
        .setDimensions(0.9F, 1.7F)
        .maxTrackingRange(10);
    public static final int[] spawnEggColors = { 10236982, 5065037 };

    private static final Ingredient BREEDING_INGREDIENT;
    private static final Ingredient ATTRACTING_INGREDIENT;
    private static final TrackedData<Integer> BOOST_TIME;
    private static final TrackedData<Boolean> SOUL_SURROUNDED;
    private static final TrackedData<Boolean> HIDING;
    private static final TrackedData<Boolean> SADDLED;
    private final SaddledComponent saddledComponent;
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE;
    private TemptGoal temptGoal;
    private EscapeDangerGoal escapeDangerGoal;

    public SoulStriderEntity(EntityType<? extends SoulStriderEntity> entityType, World world) {
        super(entityType, world);
        this.saddledComponent = new SaddledComponent(this.dataTracker, BOOST_TIME, SADDLED);
        this.inanimate = true;
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0F);
        this.setPathfindingPenalty(PathNodeType.LAVA, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0F);
    }

    public static boolean canSpawn(EntityType<SoulStriderEntity> type, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random) {
        BlockPos.Mutable mutable = blockPos.mutableCopy();

        do {
            mutable.move(Direction.UP);
        } while (worldAccess.getBlockState(mutable).isOf(Blocks.SOUL_SAND));

        return worldAccess.getBlockState(mutable).isAir();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BOOST_TIME.equals(data) && this.world.isClient) {
            this.saddledComponent.boost();
        }

        super.onTrackedDataSet(data);
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(BOOST_TIME, 0);
        this.dataTracker.startTracking(SOUL_SURROUNDED, true);
        this.dataTracker.startTracking(HIDING, false);
        this.dataTracker.startTracking(SADDLED, false);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        this.saddledComponent.toTag(tag);
        tag.putBoolean("Hiding", this.isHiding());
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.saddledComponent.fromTag(tag);
        this.setHiding(tag.getBoolean("Hiding"));
    }

    @Override
    public boolean isSaddled() {
        return this.saddledComponent.isSaddled();
    }

    @Override
    public boolean canBeSaddled() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    public void saddle(SoundCategory sound) {
        this.saddledComponent.setSaddled(true);
        if (sound != null) {
            this.world.playSoundFromEntity((PlayerEntity) null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5F, 1.0F);
        }

    }

    @Override
    protected void initGoals() {
        this.escapeDangerGoal = new EscapeDangerGoal(this, 1.65D);
        this.goalSelector.add(1, this.escapeDangerGoal);
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0D));
        this.temptGoal = new TemptGoal(this, 1.4D, false, ATTRACTING_INGREDIENT);
        this.goalSelector.add(4, this.temptGoal);
        this.goalSelector.add(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.add(7, new WanderAroundGoal(this, 1.0D, 60));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, SoulStriderEntity.class, 8.0F));
    }

    public void setSoulSurrounded(boolean soulSurrounded) {
        this.dataTracker.set(SOUL_SURROUNDED, soulSurrounded);
    }
    public boolean isSoulSurrounded() {
        return this.getVehicle() instanceof SoulStriderEntity ? ((SoulStriderEntity) this.getVehicle()).isSoulSurrounded() : this.dataTracker.get(SOUL_SURROUNDED);
    }

    @Override
    public boolean canWalkOnFluid(Fluid fluid) {
        return false;
    }

    @Override
    public Box getHardCollisionBox(Entity collidingEntity) {
        return collidingEntity.isPushable() ? collidingEntity.getBoundingBox() : null;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public double getMountedHeightOffset() {
        float f = Math.min(0.25F, this.limbDistance);
        return (double) this.getHeight() - 0.2D + (double) (0.12F * MathHelper.cos(this.limbAngle * 1.5F) * 2.0F * f);
    }

    @Override
    public boolean canBeControlledByRider() {
        Entity entity = this.getPrimaryPassenger();
        if (!(entity instanceof PlayerEntity)) {
            return false;
        } else {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            return playerEntity.getMainHandStack().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || playerEntity.getOffHandStack().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
        }
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.intersectsEntities(this);
    }

    @Override
    public Entity getPrimaryPassenger() {
        return this.getPassengerList().isEmpty() ? null : (Entity) this.getPassengerList().get(0);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d[] possibleDismounts = new Vec3d[] {
                getPassengerDismountOffset((double) this.getWidth(), (double) passenger.getWidth(), passenger.yaw),
                getPassengerDismountOffset((double) this.getWidth(), (double) passenger.getWidth(),
                        passenger.yaw - 22.5F),
                getPassengerDismountOffset((double) this.getWidth(), (double) passenger.getWidth(),
                        passenger.yaw + 22.5F),
                getPassengerDismountOffset((double) this.getWidth(), (double) passenger.getWidth(),
                        passenger.yaw - 45.0F),
                getPassengerDismountOffset((double) this.getWidth(), (double) passenger.getWidth(),
                        passenger.yaw + 45.0F) };
        Set<BlockPos> set = Sets.newLinkedHashSet();
        double maxYBound = this.getBoundingBox().maxY;
        double minYBound = this.getBoundingBox().minY - 0.5D;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int possibilities = possibleDismounts.length;

        double y;
        for (int i = 0; i < possibilities; i++) {
            Vec3d vec3d = possibleDismounts[i];
            mutable.set(this.getX() + vec3d.x, maxYBound, this.getZ() + vec3d.z);

            for (y = maxYBound; y > minYBound; y--) {
                set.add(mutable.toImmutable());
                mutable.move(Direction.DOWN);
            }
        }

        Iterator<BlockPos> posIter = set.iterator();

        while (true) {
            BlockPos blockPos;
            do {
                if (!posIter.hasNext()) {
                    return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
                }

                blockPos = (BlockPos) posIter.next();
            } while (this.world.getFluidState(blockPos).isIn(FluidTags.LAVA));

            UnmodifiableIterator<EntityPose> poseIter = passenger.getPoses().iterator();

            while (poseIter.hasNext()) {
                EntityPose entityPose = (EntityPose) poseIter.next();
                y = this.world.getCollisionHeightAt(blockPos);
                if (Dismounting.canDismountInBlock(y)) {
                    Box box = passenger.getBoundingBox(entityPose);
                    Vec3d vec3d2 = Vec3d.ofCenter(blockPos, y);
                    if (Dismounting.canPlaceEntityAt(this.world, passenger, box.offset(vec3d2))) {
                        passenger.setPose(entityPose);
                        return vec3d2;
                    }
                }
            }
        }
    }

    @Override
    public void travel(Vec3d movementInput) {
        this.setMovementSpeed(this.isHiding() ? 0 : this.getSpeed());
        this.travel(this, this.saddledComponent, movementInput);
    }

    public float getSpeed() {
        return (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (!this.isSoulSurrounded() ? 0.66F : 1.0F);
    }

    @Override
    public float getSaddledSpeed() {
        return (float) this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * (!this.isSoulSurrounded() ? 0.23F : 0.55F);
    }

    @Override
    public void setMovementInput(Vec3d movementInput) {
        super.travel(movementInput);
    }

    @Override
    protected float calculateNextStepSoundDistance() {
        return this.distanceTraveled + 0.6F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.isNearSouls() ? SoundEvents.ENTITY_STRIDER_STEP_LAVA : SoundEvents.ENTITY_STRIDER_STEP, 1.0F, 1.0F);
    }

    @Override
    public boolean consumeOnAStickItem() {
        return this.saddledComponent.boost(this.getRandom());
    }

    @Override
    protected float getVelocityMultiplier() {
        return this.isNearSouls() ? 1.0F : super.getVelocityMultiplier();
    }

    public boolean isNearSouls() {
        BlockPos pos = this.getBlockPos();
        return this.isOnSoulBlock(pos) || this.world.getBiome(pos) == Biomes.SOUL_SAND_VALLEY;
    }

    public boolean isOnSoulBlock(BlockPos pos) {
        return this.world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS);
    }

    @Override
    public void tick() {
        if (this.isTempting() && this.random.nextInt(140) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_HAPPY, 1.0F, this.getSoundPitch());
        } else if (this.isEscapingDanger() && this.random.nextInt(60) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_RETREAT, 1.0F, this.getSoundPitch());
        }

        this.setSoulSurrounded(isNearSouls());
        super.tick();
        this.checkBlockCollision();
    }

    @Override
    protected void mobTick() {
        super.mobTick();

        boolean isHideSafe = !isBaby() && !isTempting() && this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) == null && this.getPassengerList().size() == 0 && this.isOnSoulBlock(this.getBlockPos());
        if (this.isHiding() && !isHideSafe) this.setHiding(false);
            else if (this.random.nextInt(100) == 0 && isHideSafe) this.setHiding(true);
    }

    public boolean isHiding() {
        return this.dataTracker.get(HIDING);
    }
    public void setHiding(boolean hiding) {
        this.dataTracker.set(HIDING, hiding);
        this.calculateDimensions();
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        EntityDimensions entityDimensions = super.getDimensions(pose);
        return !isHiding() ? entityDimensions : EntityDimensions.fixed(entityDimensions.width, entityDimensions.height - (float) 1.08D);
    }

    private boolean isEscapingDanger() {
        return this.escapeDangerGoal != null && this.escapeDangerGoal.isActive();
    }

    private boolean isTempting() {
        return this.temptGoal != null && this.temptGoal.isActive();
    }

    @Override
    protected boolean movesIndependently() {
        return true;
    }

    public static DefaultAttributeContainer.Builder createStriderAttributes() {
        return MobEntity.createMobAttributes()
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17499999701976776D)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return !this.isEscapingDanger() && !this.isTempting() ? SoundEvents.ENTITY_STRIDER_AMBIENT : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().isEmpty() && !this.isSubmergedIn(FluidTags.LAVA);
    }

    @Override
    public boolean hurtByWater() {
        return true;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SoulStriderEntity.Navigation(this, world);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) ? 10.0F : 0.0F;
    }

    @Override
    public void breed(World world, AnimalEntity other) {
        PassiveEntity wisp = SSEntities.WISP.create(world);
        if (wisp != null) {
            ServerPlayerEntity serverPlayerEntity = this.getLovingPlayer();
            if (serverPlayerEntity == null && other.getLovingPlayer() != null) {
                serverPlayerEntity = other.getLovingPlayer();
            }

            if (serverPlayerEntity != null) {
                serverPlayerEntity.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(serverPlayerEntity, this, other, wisp);
            }

            this.setBreedingAge(6000);
            other.setBreedingAge(6000);
            this.resetLoveTicks();
            other.resetLoveTicks();
            wisp.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
            world.spawnEntity(wisp);
            world.sendEntityStatus(this, (byte) 18);
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
                world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return BREEDING_INGREDIENT.test(stack);
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.isSaddled()) {
            this.dropItem(Items.SADDLE);
        }

    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean playerHoldingBreedingItem = this.isBreedingItem(player.getStackInHand(hand));
        if (!playerHoldingBreedingItem && this.isSaddled() && !this.hasPassengers()) {
            if (!this.world.isClient) {
                player.startRiding(this);
            }

            return ActionResult.success(this.world.isClient);
        } else {
            ActionResult actionResult = super.interactMob(player, hand);
            if (!actionResult.isAccepted()) {
                ItemStack itemStack = player.getStackInHand(hand);
                return itemStack.getItem() == Items.SADDLE ? itemStack.useOnEntity(player, this, hand) : ActionResult.PASS;
            } else {
                if (playerHoldingBreedingItem && !this.isSilent()) {
                    this.world.playSound((PlayerEntity) null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                return actionResult;
            }
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Vec3d method_29919() {
        return new Vec3d(0.0D, (double) (0.6F * this.getStandingEyeHeight()), (double) (this.getWidth() * 0.4F));
    }

    @Override
    public EntityData initialize(WorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CompoundTag entityTag) {
        EntityData entityData2 = null;
        SoulStriderEntity.StriderData.RiderType riderType4;
        if (entityData instanceof SoulStriderEntity.StriderData) {
            riderType4 = ((SoulStriderEntity.StriderData) entityData).type;
        } else if (!this.isBaby()) {
            if (this.random.nextInt(30) == 0) {
                riderType4 = SoulStriderEntity.StriderData.RiderType.PIGLIN_RIDER;
                entityData2 = new ZombieEntity.ZombieData(ZombieEntity.method_29936(this.random), false);
            } else if (this.random.nextInt(10) == 0) {
                riderType4 = SoulStriderEntity.StriderData.RiderType.BABY_RIDER;
            } else {
                riderType4 = SoulStriderEntity.StriderData.RiderType.NO_RIDER;
            }

            entityData = new SoulStriderEntity.StriderData(riderType4);
            ((PassiveEntity.PassiveData) entityData)
                    .setBabyChance(riderType4 == SoulStriderEntity.StriderData.RiderType.NO_RIDER ? 0.5F : 0.0F);
        } else {
            riderType4 = SoulStriderEntity.StriderData.RiderType.NO_RIDER;
        }

        MobEntity mobEntity = null;
        if (riderType4 == SoulStriderEntity.StriderData.RiderType.BABY_RIDER) {
            SoulStriderEntity striderEntity = SSEntities.SOUL_STRIDER.create(world.getWorld());
            if (striderEntity != null) {
                mobEntity = striderEntity;
                striderEntity.setBreedingAge(-24000);
            }
        } else if (riderType4 == SoulStriderEntity.StriderData.RiderType.PIGLIN_RIDER) {
            ZombifiedPiglinEntity zombifiedPiglinEntity = (ZombifiedPiglinEntity) EntityType.ZOMBIFIED_PIGLIN
                    .create(world.getWorld());
            if (zombifiedPiglinEntity != null) {
                mobEntity = zombifiedPiglinEntity;
                this.saddle((SoundCategory) null);
            }
        }

        if (mobEntity != null) {
            ((MobEntity) mobEntity).refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, 0.0F);
            ((MobEntity) mobEntity).initialize(world, difficulty, SpawnReason.JOCKEY, entityData2, (CompoundTag) null);
            ((MobEntity) mobEntity).startRiding(this, true);
            world.spawnEntity((Entity) mobEntity);
        }

        return super.initialize(world, difficulty, spawnReason, (EntityData) entityData, entityTag);
    }

    static {
        BREEDING_INGREDIENT = Ingredient.ofItems(Items.SOUL_SOIL);
        ATTRACTING_INGREDIENT = Ingredient.ofItems(Items.SOUL_SOIL, Items.WARPED_FUNGUS_ON_A_STICK);
        BOOST_TIME = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
        SOUL_SURROUNDED = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        HIDING = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        SADDLED = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        CLOSE_PLAYER_PREDICATE = new TargetPredicate().setBaseMaxDistance(8.0D).includeTeammates();
    }

    static class Navigation extends MobNavigation {
        Navigation(SoulStriderEntity entity, World world) {
            super(entity, world);
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = new LandPathNodeMaker();
            return new PathNodeNavigator(this.nodeMaker, range);
        }

        @Override
        protected boolean canWalkOnPath(PathNodeType pathType) {
            return !(pathType != PathNodeType.LAVA && pathType != PathNodeType.DAMAGE_FIRE && pathType != PathNodeType.DANGER_FIRE) ? super.canWalkOnPath(pathType) : true;
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            return this.world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBiome(pos) == Biomes.SOUL_SAND_VALLEY || super.isValidPosition(pos);
        }
    }

    public static class StriderData extends PassiveEntity.PassiveData {
        public final SoulStriderEntity.StriderData.RiderType type;

        public StriderData(SoulStriderEntity.StriderData.RiderType type) {
            this.type = type;
        }

        public static enum RiderType {
            NO_RIDER, BABY_RIDER, PIGLIN_RIDER;
        }
    }

    @Override
    public PassiveEntity createChild(PassiveEntity mate) {
        return null;
    }
}
