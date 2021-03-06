package net.teamhollow.soulstriders.entity.soul_strider;

import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeKeys;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntity;
import net.teamhollow.soulstriders.init.SSBlocks;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.init.SSItems;

import java.util.*;
import java.util.function.Consumer;

public class SoulStriderEntity extends AnimalEntity implements ItemSteerable, Saddleable, Shearable {
    public static final String id = "soul_strider";

    private static final Ingredient BREEDING_INGREDIENT = Ingredient.ofItems(SSItems.SOUL_MOTH_IN_A_BOTTLE);
    private static final Ingredient ATTRACTING_INGREDIENT = Ingredient.ofItems(SSItems.SOUL_MOTH_IN_A_BOTTLE, SSItems.SOUL_MOTH_ON_A_STICK);
    private static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> SOUL_SURROUNDED = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> HIDING = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> NO_BULB_TICKS = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> SADDLED = DataTracker.registerData(SoulStriderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private final SaddledComponent saddledComponent;
    private static final TargetPredicate CLOSE_PLAYER_PREDICATE = new TargetPredicate().setBaseMaxDistance(8.0D).includeTeammates();
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

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<SoulStriderEntity> type, WorldAccess worldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random) {
        BlockPos.Mutable mutable = blockPos.mutableCopy();

        do mutable.move(Direction.UP);
            while (worldAccess.getBlockState(mutable).isOf(Blocks.SOUL_SAND));

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
        this.dataTracker.startTracking(NO_BULB_TICKS, 0);
        this.dataTracker.startTracking(SADDLED, false);
    }

    @Override
    public void writeCustomDataToTag(CompoundTag tag) {
        super.writeCustomDataToTag(tag);
        this.saddledComponent.toTag(tag);
        tag.putBoolean("Hiding", this.isHiding());
        tag.putInt("NoBulbTicks", this.getNoBulbTicks());
    }

    @Override
    public void readCustomDataFromTag(CompoundTag tag) {
        super.readCustomDataFromTag(tag);
        this.saddledComponent.fromTag(tag);
        this.setHiding(tag.getBoolean("Hiding"));
        this.setNoBulbTicks(tag.getInt("NoBulbTicks"));
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
            this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5F, 1.0F);
        }

    }

    @Override
    protected void initGoals() {
        this.escapeDangerGoal = new EscapePlayersGoal(this, 3.0D);
        this.goalSelector.add(1, this.escapeDangerGoal);
        this.goalSelector.add(3, new AnimalMateGoal(this, 1.0D));
        this.temptGoal = new TemptGoal(this, 1.4D, false, ATTRACTING_INGREDIENT);
        this.goalSelector.add(4, this.temptGoal);
        this.goalSelector.add(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.add(7, new WanderAroundGoal(this, 1.0D, 60));
        this.goalSelector.add(8, new LookAtEntityGoal(this, 8.0F, 0.02F));
        this.goalSelector.add(8, new EatSoulMothGoal(this, 1.5F));
        this.goalSelector.add(8, new LookAroundGoal(this));
    }

    public void setSoulSurrounded(boolean soulSurrounded) {
        this.dataTracker.set(SOUL_SURROUNDED, soulSurrounded);
    }
    public boolean isSoulSurrounded() {
        return this.getVehicle() instanceof SoulStriderEntity
                ? ((SoulStriderEntity) this.getVehicle()).isSoulSurrounded()
                : this.dataTracker.get(SOUL_SURROUNDED);
    }

    @Override
    public boolean canWalkOnFluid(Fluid fluid) {
        return false;
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
            return playerEntity.getMainHandStack().getItem() == SSItems.SOUL_MOTH_ON_A_STICK || playerEntity.getOffHandStack().getItem() == SSItems.SOUL_MOTH_ON_A_STICK;
        }
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.intersectsEntities(this);
    }

    @Override
    public Entity getPrimaryPassenger() {
        return this.getPassengerList().isEmpty() ? null : this.getPassengerList().get(0);
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        Vec3d[] possibleDismounts = new Vec3d[] {
                getPassengerDismountOffset(this.getWidth(), passenger.getWidth(), passenger.yaw),
                getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                        passenger.yaw - 22.5F),
                getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                        passenger.yaw + 22.5F),
                getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                        passenger.yaw - 45.0F),
                getPassengerDismountOffset(this.getWidth(), passenger.getWidth(),
                        passenger.yaw + 45.0F) };
        Set<BlockPos> set = Sets.newLinkedHashSet();
        double maxYBound = this.getBoundingBox().maxY;
        double minYBound = this.getBoundingBox().minY - 0.5D;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        double y;
        for (Vec3d vec3d : possibleDismounts) {
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

                blockPos = posIter.next();
            } while (this.world.getFluidState(blockPos).isIn(FluidTags.LAVA));

            for (EntityPose entityPose : passenger.getPoses()) {
                y = this.world.getDismountHeight(blockPos);
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
        return this.isOnSoulBlock(pos) || Objects.equals(world.method_31081(pos), Optional.of(BiomeKeys.SOUL_SAND_VALLEY));
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

        if (this.isAlive()) {
            int noBulbTicks = this.getNoBulbTicks();
            if (noBulbTicks < 0) {
                noBulbTicks++;
                this.setNoBulbTicks(noBulbTicks);
            } else if (noBulbTicks > 0) {
                noBulbTicks--;
                this.setNoBulbTicks(noBulbTicks);
            }

            boolean isHideSafe = hasBulb() && !isBaby() && !isTempting()
                    && this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) == null
                    && this.getPassengerList().size() == 0 && this.isOnSoulBlock(this.getBlockPos());
            if (this.isHiding() && !isHideSafe)
                this.setHiding(false);
            else if (this.random.nextInt(100) == 0 && isHideSafe)
                this.setHiding(true);
        }
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
    public void breed(ServerWorld world, AnimalEntity other) {
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
            wisp.setPersistent();
            world.spawnEntity(wisp);
            world.sendEntityStatus(this, (byte) 18);
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
                world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
            
            this.setNoBulbTicks(6000);
        }
    }

    public int getNoBulbTicks() {
        return this.dataTracker.get(NO_BULB_TICKS);
    }
    public void setNoBulbTicks(int noBulbTicks) {
        this.dataTracker.set(NO_BULB_TICKS, noBulbTicks);
    }
    public boolean hasBulb() {
        return this.getNoBulbTicks() == 0;
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
    public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
        return false;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getStackInHand(hand);
        boolean playerHoldingBreedingItem = handStack.getItem() == SSItems.SOUL_MOTH_IN_A_BOTTLE;
        boolean playerHoldingShears = handStack.getItem() == Items.SHEARS;
        if (!playerHoldingBreedingItem && !playerHoldingShears && this.isSaddled() && !this.hasPassengers()) {
            if (!this.world.isClient) {
                player.startRiding(this);
            }

            return ActionResult.success(this.world.isClient);
        } else {
            ActionResult actionResult = super.interactMob(player, hand);
            if (!actionResult.isAccepted()) {
                if (playerHoldingShears) {
                    if (!this.world.isClient && this.isShearable()) {
                        this.sheared(SoundCategory.PLAYERS);
                        handStack.damage(1, player, (Consumer<LivingEntity>) ((playerEntity) -> playerEntity.sendToolBreakStatus(hand)));

                        return ActionResult.SUCCESS;
                    } else
                        return ActionResult.CONSUME;
                }

                return handStack.getItem() == Items.SADDLE ? handStack.useOnEntity(player, this, hand) : ActionResult.PASS;
            } else if (!this.world.isClient) {
                if (playerHoldingBreedingItem && !this.isSilent()) {
                    this.world.playSound(player, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
                }

                if (!player.isCreative())
                    player.setStackInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
            }

            return actionResult;
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Vec3d method_29919() {
        return new Vec3d(0.0D, 0.6F * this.getStandingEyeHeight(), this.getWidth() * 0.4F);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CompoundTag entityTag) {
        if (!this.isBaby()) {
            if (this.random.nextInt(30) == 0) {
                MobEntity mobEntity = EntityType.ZOMBIFIED_PIGLIN.create(world.toServerWorld());
                assert mobEntity != null;
                entityData = this.method_30336(world, difficulty, mobEntity, new ZombieEntity.ZombieData(ZombieEntity.method_29936(this.random), false));
                mobEntity.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.saddle(null);
            } else if (this.random.nextInt(10) == 0) {
                PassiveEntity passiveEntity = SSEntities.SOUL_STRIDER.create(world.toServerWorld());
                assert passiveEntity != null;
                passiveEntity.setBreedingAge(-24000);
                entityData = this.method_30336(world, difficulty, passiveEntity, null);
            } else {
                entityData = new PassiveData(0.5F);
            }

        }
        return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
    }
    private EntityData method_30336(ServerWorldAccess serverWorldAccess, LocalDifficulty localDifficulty, MobEntity mobEntity, EntityData entityData) {
        mobEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, 0.0F);
        mobEntity.initialize(serverWorldAccess, localDifficulty, SpawnReason.JOCKEY, entityData, null);
        mobEntity.startRiding(this, true);
        return new PassiveEntity.PassiveData(0.0F);
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
            return pathType != PathNodeType.LAVA && pathType != PathNodeType.DAMAGE_FIRE && pathType != PathNodeType.DANGER_FIRE || super.canWalkOnPath(pathType);
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            return this.world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) || Objects.equals(world.method_31081(pos), Optional.of(BiomeKeys.SOUL_SAND_VALLEY)) || super.isValidPosition(pos);
        }
    }

    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity mate) {
        return null;
    }

    static class LookAtEntityGoal extends Goal {
        protected final MobEntity mob;
        protected Entity target;
        protected final float range;
        private int lookTime;
        protected final float chance;
        protected Class<? extends LivingEntity> targetType;
        protected final TargetPredicate targetPredicate;

        public LookAtEntityGoal(MobEntity mob, float range, float chance) {
            this.mob = mob;
            this.range = range;
            this.chance = chance;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
            if (targetType == PlayerEntity.class) {
                this.targetPredicate = (new TargetPredicate()).setBaseMaxDistance(range).includeTeammates().includeInvulnerable().ignoreEntityTargetRules().setPredicate((livingEntity) -> EntityPredicates.rides(mob).test(livingEntity));
            } else {
                this.targetPredicate = (new TargetPredicate()).setBaseMaxDistance(range).includeTeammates().includeInvulnerable().ignoreEntityTargetRules();
            }

        }

        @Override
        public boolean canStart() {
            SoulStriderEntity mob = (SoulStriderEntity)this.mob;
            this.targetType = mob.isHiding() ? SoulMothEntity.class : (mob.random.nextInt(5) == 0 ? PlayerEntity.class : SoulStriderEntity.class);

            if (this.mob.getRandom().nextFloat() >= this.chance) {
                return false;
            } else {
                if (this.mob.getTarget() != null) {
                    this.target = this.mob.getTarget();
                }

                if (this.targetType == PlayerEntity.class) {
                    this.target = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(),
                            this.mob.getEyeY(), this.mob.getZ());
                } else {
                    this.target = this.mob.world.getClosestEntityIncludingUngeneratedChunks(this.targetType,
                            this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(),
                            this.mob.getBoundingBox().expand(this.range, 3.0D, this.range));
                }

                return this.target != null;
            }
        }

        @Override
        public boolean shouldContinue() {
            if (!this.target.isAlive()) {
                return false;
            } else if (this.mob.squaredDistanceTo(this.target) > (double) (this.range * this.range)) {
                return false;
            } else {
                return this.lookTime > 0;
            }
        }

        @Override
        public void start() {
            this.lookTime = 40 + this.mob.getRandom().nextInt(40);
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void tick() {
            this.mob.getLookControl().lookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
            --this.lookTime;
        }
    }

    class EatSoulMothGoal extends Goal {
        protected SoulStriderEntity mob;
        protected float range;

        public EatSoulMothGoal(SoulStriderEntity mob, float range) {
            super();
            this.mob = mob;
            this.range = range;
        }

        @Override
        public void tick() {
            if (random.nextInt(40) == 0) {
                eatMoth(this.mob.world.getClosestEntityIncludingUngeneratedChunks(SoulMothEntity.class, new TargetPredicate().setBaseMaxDistance(range).includeTeammates().includeInvulnerable().ignoreEntityTargetRules(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.mob.getBoundingBox().expand(this.range, this.range, this.range)));
            }

            super.tick();
        }

        private void eatMoth(SoulMothEntity entity) {
            if (entity != null) {
                entity.playSound(SoundEvents.ENTITY_FOX_EAT, 1.0F, 1.0F);
                entity.remove();
            }
        }

        @Override
        public boolean canStart() {
            return mob.isHiding();
        }
    }

    static class EscapePlayersGoal extends EscapeDangerGoal {
        public EscapePlayersGoal(PathAwareEntity mob, double speed) {
            super(mob, speed);
        }

        @Override
        public boolean canStart() {
            if (mob.world.getClosestPlayer(
                new TargetPredicate()
                    .setBaseMaxDistance(15.0D)
                    .includeTeammates()
                    .includeInvulnerable()
                    .ignoreEntityTargetRules()
                    .setPredicate((livingEntity) -> EntityPredicates.rides(mob).test(livingEntity)), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) == null && !mob.isOnFire()) {
                return false;
            } else {
                if (mob.isOnFire()) {
                    BlockPos blockPos = this.locateClosestWater(mob.world, mob, 5, 4);
                    if (blockPos != null) {
                        this.targetX = blockPos.getX();
                        this.targetY = blockPos.getY();
                        this.targetZ = blockPos.getZ();
                        return true;
                    }
                }

                return this.findTarget();
            }
        }
    }

    @Override
    public void sheared(SoundCategory shearedSoundCategory) {
        this.world.playSoundFromEntity(null, this, SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);

        this.setNoBulbTicks(6000);
        this.setBreedingAge(6000);

        ItemEntity itemEntity = this.dropItem(SSBlocks.SOUL_STRIDER_BULB, 1);
        if (itemEntity != null) {
            itemEntity.setVelocity(
                itemEntity.getVelocity().add(
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                    (this.random.nextFloat() * 0.05F),
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.1F
                )
            );
        }
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && this.hasBulb() && !this.isBaby();
    }
}
