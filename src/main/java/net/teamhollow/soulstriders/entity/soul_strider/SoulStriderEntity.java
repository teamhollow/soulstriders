package net.teamhollow.soulstriders.entity.soul_strider;

import com.google.common.collect.Sets;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.*;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntity;
import net.teamhollow.soulstriders.init.SSBlocks;
import net.teamhollow.soulstriders.init.SSEntities;
import net.teamhollow.soulstriders.init.SSItems;

import javax.annotation.Nullable;
import java.util.*;

public class SoulStriderEntity extends AnimalEntity implements IRideable, IEquipable, IShearable {
    public static final String id = "soul_strider";

    private static final Ingredient BREEDING_INGREDIENT = Ingredient.fromItems(SSItems.SOUL_MOTH_IN_A_BOTTLE);
    private static final Ingredient ATTRACTING_INGREDIENT = Ingredient.fromItems(SSItems.SOUL_MOTH_IN_A_BOTTLE, SSItems.SOUL_MOTH_ON_A_STICK);
    private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.createKey(SoulStriderEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SOUL_SURROUNDED = EntityDataManager.createKey(SoulStriderEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> HIDING = EntityDataManager.createKey(SoulStriderEntity.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> NO_BULB_TICKS = EntityDataManager.createKey(SoulStriderEntity.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SADDLED = EntityDataManager.createKey(SoulStriderEntity.class, DataSerializers.BOOLEAN);
    private final BoostHelper boostHelper;
    private static final EntityPredicate CLOSE_PLAYER_PREDICATE = new EntityPredicate().setDistance(8.0D).allowFriendlyFire().setCustomPredicate(EntityPredicates.CAN_AI_TARGET::test);
    private TemptGoal temptGoal;
    private PanicGoal escapeDangerGoal;

    public SoulStriderEntity(EntityType<? extends SoulStriderEntity> entityType, World world) {
        super(entityType, world);
        this.boostHelper = new BoostHelper(this.getDataManager(), BOOST_TIME, SADDLED);
        this.preventEntitySpawning = true;
        this.setPathPriority(PathNodeType.WATER, -1.0F);
        this.setPathPriority(PathNodeType.LAVA, -1.0F);
        this.setPathPriority(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathPriority(PathNodeType.DAMAGE_FIRE, -1.0F);
    }

    @SuppressWarnings("unused")
    public static boolean canSpawn(EntityType<SoulStriderEntity> type, IWorld worldAccess, SpawnReason spawnReason, BlockPos blockPos, Random random) {
        BlockPos.Mutable mutable = blockPos.toMutable();

        do mutable.move(Direction.UP);
        while (worldAccess.getBlockState(mutable).isIn(Blocks.SOUL_SAND));

        return worldAccess.getBlockState(mutable).isAir();
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        if (BOOST_TIME.equals(key) && this.world.isRemote) {
            this.boostHelper.updateData();
        }
        super.notifyDataManagerChange(key);
    }

    @Override
    protected void registerData() {
        super.registerData();
        this.dataManager.register(BOOST_TIME, 0);
        this.dataManager.register(SOUL_SURROUNDED, true);
        this.dataManager.register(HIDING, false);
        this.dataManager.register(NO_BULB_TICKS, 0);
        this.dataManager.register(SADDLED, false);
    }

    @Override
    public void writeAdditional(CompoundNBT tag) {
        super.writeAdditional(tag);
        this.boostHelper.setSaddledToNBT(tag);
        tag.putBoolean("Hiding", this.isHiding());
        tag.putInt("NoBulbTicks", this.getNoBulbTicks());
    }

    @Override
    public void readAdditional(CompoundNBT tag) {
        super.readAdditional(tag);
        this.boostHelper.setSaddledFromNBT(tag);
        this.setHiding(tag.getBoolean("Hiding"));
        this.setNoBulbTicks(tag.getInt("NoBulbTicks"));
    }

    @Override
    public boolean isHorseSaddled() {
        return this.boostHelper.getSaddled();
    }

    @Override
    public boolean func_230264_L__() {
        return this.isAlive() && !this.isChild();
    }

    @Override
    public void func_230266_a_(SoundCategory sound) {
        this.boostHelper.setSaddledFromBoolean(true);
        if (sound != null) {
            this.world.playMovingSound(null, this, SoundEvents.ENTITY_STRIDER_SADDLE, sound, 0.5F, 1.0F);
        }

    }

    @Override
    protected void registerGoals() {
        this.escapeDangerGoal = new EscapePlayersGoal(this, 3.0D);
        this.goalSelector.addGoal(1, this.escapeDangerGoal);
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.temptGoal = new TemptGoal(this, 1.4D, false, ATTRACTING_INGREDIENT);
        this.goalSelector.addGoal(4, this.temptGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(7, new RandomWalkingGoal(this, 1.0D, 60));
        this.goalSelector.addGoal(8, new LookAtEntityGoal(this, 8.0F, 0.02F));
        this.goalSelector.addGoal(8, new EatSoulMothGoal(this, 1.5F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
    }

    public void setSoulSurrounded(boolean soulSurrounded) {
        this.dataManager.set(SOUL_SURROUNDED, soulSurrounded);
    }

    public boolean isSoulSurrounded() {
        return this.getRidingEntity() instanceof SoulStriderEntity
               ? ((SoulStriderEntity) this.getRidingEntity()).isSoulSurrounded()
               : this.dataManager.get(SOUL_SURROUNDED);
    }

    @Override
    public boolean func_230285_a_(Fluid fluid) {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return true;
    }


    @Override
    public double getMountedYOffset() {
        float f = Math.min(0.25F, this.limbSwingAmount);
        return (double) this.getHeight() - 0.2D + (double) (0.12F * MathHelper.cos(this.limbSwing * 1.5F) * 2.0F * f);
    }

    @Override
    public boolean canBeSteered() {
        Entity entity = this.getControllingPassenger();
        if (!(entity instanceof PlayerEntity)) {
            return false;
        } else {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            return playerEntity.getHeldItemMainhand().getItem() == SSItems.SOUL_MOTH_ON_A_STICK || playerEntity.getHeldItemOffhand().getItem() == SSItems.SOUL_MOTH_ON_A_STICK;
        }
    }

    @Override
    public boolean isNotColliding(IWorldReader worldIn) {
        return worldIn.checkNoEntityCollision(this);
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
    }

    @Override
    public Vector3d func_230268_c_(LivingEntity passenger) {
        Vector3d[] possibleDismounts = {
            func_233559_a_(this.getWidth(), passenger.getWidth(), passenger.rotationYaw),
            func_233559_a_(this.getWidth(), passenger.getWidth(), passenger.rotationYaw - 22.5F),
            func_233559_a_(this.getWidth(), passenger.getWidth(), passenger.rotationYaw + 22.5F),
            func_233559_a_(this.getWidth(), passenger.getWidth(), passenger.rotationYaw - 45.0F),
            func_233559_a_(this.getWidth(), passenger.getWidth(), passenger.rotationYaw + 45.0F)
        };
        Set<BlockPos> set = Sets.newLinkedHashSet();
        double maxYBound = this.getBoundingBox().maxY;
        double minYBound = this.getBoundingBox().minY - 0.5D;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        double y;
        for (Vector3d vec3d : possibleDismounts) {
            mutable.setPos(this.getPosX() + vec3d.x, maxYBound, this.getPosZ() + vec3d.z);

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
                    return new Vector3d(this.getPosX(), this.getBoundingBox().maxY, this.getPosZ());
                }

                blockPos = posIter.next();
            } while (this.world.getFluidState(blockPos).isTagged(FluidTags.LAVA));

            for (Pose entityPose : passenger.getAvailablePoses()) {
                y = this.world.func_242403_h(blockPos);
                if (TransportationHelper.func_234630_a_(y)) {
                    AxisAlignedBB box = passenger.getPoseAABB(entityPose);
                    Vector3d vec3d2 = Vector3d.copyCenteredWithVerticalOffset(blockPos, y);
                    if (TransportationHelper.func_234631_a_(this.world, passenger, box.offset(vec3d2))) {
                        passenger.setPose(entityPose);
                        return vec3d2;
                    }
                }
            }
        }
    }

    @Override
    public void travel(Vector3d movementInput) {
        this.setAIMoveSpeed(this.isHiding() ? 0 : this.getSpeed());
        this.ride(this, this.boostHelper, movementInput);
    }

    public float getSpeed() {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (!this.isSoulSurrounded() ? 0.66F : 1.0F);
    }

    @Override
    public float getMountedSpeed() {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (!this.isSoulSurrounded() ? 0.23F : 0.55F);
    }

    @Override
    public void travelTowards(Vector3d movementInput) {
        super.travel(movementInput);
    }

    @Override
    protected float determineNextStepDistance() {
        return this.distanceWalkedOnStepModified + 0.6F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.isNearSouls() ? SoundEvents.ENTITY_STRIDER_STEP_LAVA : SoundEvents.ENTITY_STRIDER_STEP, 1.0F, 1.0F);
    }

    @Override
    public boolean boost() {
        return this.boostHelper.boost(this.getRNG());
    }

    @Override
    protected float getSpeedFactor() {
        return this.isNearSouls() ? 1.0F : super.getSpeedFactor();
    }

    public boolean isNearSouls() {
        BlockPos pos = this.getPosition();
        return this.isOnSoulBlock(pos) || Objects.equals(world.func_242406_i(pos), Optional.of(Biomes.SOUL_SAND_VALLEY));
    }

    public boolean isOnSoulBlock(BlockPos pos) {
        return this.world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS);
    }

    @Override
    public void tick() {
        if (this.isTempting() && this.rand.nextInt(140) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_HAPPY, 1.0F, this.getSoundPitch());
        } else if (this.isEscapingDanger() && this.rand.nextInt(60) == 0) {
            this.playSound(SoundEvents.ENTITY_STRIDER_RETREAT, 1.0F, this.getSoundPitch());
        }

        this.setSoulSurrounded(isNearSouls());
        super.tick();
        this.doBlockCollisions();
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();

        if (this.isAlive()) {
            int noBulbTicks = this.getNoBulbTicks();
            if (noBulbTicks < 0) {
                noBulbTicks++;
                this.setNoBulbTicks(noBulbTicks);
            } else if (noBulbTicks > 0) {
                noBulbTicks--;
                this.setNoBulbTicks(noBulbTicks);
            }

            boolean isHideSafe = hasBulb() && !isChild() && !isTempting()
                                     && this.world.getClosestPlayer(CLOSE_PLAYER_PREDICATE, this) == null
                                     && this.getPassengers().size() == 0 && this.isOnSoulBlock(this.getPosition());
            if (this.isHiding() && !isHideSafe)
                this.setHiding(false);
            else if (this.getRNG().nextInt(100) == 0 && isHideSafe)
                this.setHiding(true);
        }
    }

    public boolean isHiding() {
        return this.dataManager.get(HIDING);
    }

    public void setHiding(boolean hiding) {
        this.dataManager.set(HIDING, hiding);
        this.recalculateSize();
    }

    @Override
    public EntitySize getSize(Pose pose) {
        EntitySize entityDimensions = super.getSize(pose);
        return !isHiding() ? entityDimensions : EntitySize.flexible(entityDimensions.width, entityDimensions.height - (float) 1.08D);
    }

    private boolean isEscapingDanger() {
        return this.escapeDangerGoal != null && this.escapeDangerGoal.isRunning();
    }

    private boolean isTempting() {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    @Override
    public boolean getMovementSpeed() {
        return super.getMovementSpeed();
    }

    @Override
    protected boolean func_230286_q_() {
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
    protected boolean canFitPassenger(Entity passenger) {
        return this.getPassengers().isEmpty() && !this.areEyesInFluid(FluidTags.LAVA);
    }

    @Override
    protected PathNavigator createNavigator(World world) {
        return new Navigation(this, world);
    }

    @Override
    public float getBlockPathWeight(BlockPos pos, IWorldReader world) {
        return world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) ? 10.0F : 0.0F;
    }

    @Override
    public void func_234177_a_(ServerWorld world, AnimalEntity other) {
        AgeableEntity wisp = SSEntities.WISP.create(world);
        if (wisp != null) {
            ServerPlayerEntity serverPlayerEntity = this.getLoveCause();
            if (serverPlayerEntity == null && other.getLoveCause() != null) {
                serverPlayerEntity = other.getLoveCause();
            }

            if (serverPlayerEntity != null) {
                serverPlayerEntity.addStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayerEntity, this, other, wisp);
            }

            this.setGrowingAge(6000);
            other.setGrowingAge(6000);
            this.resetInLove();
            other.resetInLove();

            wisp.setPositionAndRotation(this.getPosX(), this.getPosY(), this.getPosZ(), 0.0F, 0.0F);
            wisp.enablePersistence();
            world.addEntity(wisp);
            world.setEntityState(this, (byte) 18);
            if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT))
                world.addEntity(new ExperienceOrbEntity(world, this.getPosX(), this.getPosY(), this.getPosZ(), this.getRNG().nextInt(7) + 1));

            this.setNoBulbTicks(6000);
        }
    }

    public int getNoBulbTicks() {
        return this.dataManager.get(NO_BULB_TICKS);
    }

    public void setNoBulbTicks(int noBulbTicks) {
        this.dataManager.set(NO_BULB_TICKS, noBulbTicks);
    }

    public boolean hasBulb() {
        return this.getNoBulbTicks() == 0;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == SSItems.SOUL_MOTH_IN_A_BOTTLE;
    }

    @Override
    protected void dropInventory() {
        super.dropInventory();
        if (this.isHorseSaddled()) {
            this.entityDropItem(Items.SADDLE);
        }
    }

    @Override
    public boolean onLivingFall(float fallDistance, float damageMultiplier) {
        return false;
    }

    @Override
    public ActionResultType func_230254_b_(PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getHeldItem(hand);
        boolean playerHoldingBreedingItem = handStack.getItem() == SSItems.SOUL_MOTH_IN_A_BOTTLE;
        boolean playerHoldingShears = handStack.getItem() == Items.SHEARS;
        if (!playerHoldingBreedingItem && !playerHoldingShears && this.isHorseSaddled() && !this.isBeingRidden()) {
            if (!this.world.isRemote) {
                player.startRiding(this);
            }

            return ActionResultType.func_233537_a_(this.world.isRemote);
        } else {
            ActionResultType actionResult = super.func_230254_b_(player, hand);
            if (!actionResult.isSuccessOrConsume()) {
                if (playerHoldingShears) {
                    if (!this.world.isRemote && this.isShearable()) {
                        this.shear(SoundCategory.PLAYERS);
                        handStack.damageItem(1, player, playerEntity -> playerEntity.sendBreakAnimation(hand));

                        return ActionResultType.SUCCESS;
                    } else
                        return ActionResultType.CONSUME;
                }

                return handStack.getItem() == Items.SADDLE ? handStack.interactWithEntity(player, this, hand) : ActionResultType.PASS;
            } else if (!this.world.isRemote) {
                if (playerHoldingBreedingItem && !this.isSilent()) {
                    this.world.playSound(player, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_STRIDER_EAT, this.getSoundCategory(), 1.0F, 1.0F + (this.getRNG().nextFloat() - this.getRNG().nextFloat()) * 0.2F);
                }

                if (!player.isCreative())
                    player.setHeldItem(hand, new ItemStack(Items.GLASS_BOTTLE));
            }

            return actionResult;
        }
    }



    @Override
    @OnlyIn(Dist.CLIENT)
    public Vector3d func_241205_ce_() {
        return new Vector3d(0.0D, 0.6F * this.getEyeHeight(), this.getWidth() * 0.4F);
    }

    @Override
    public ILivingEntityData onInitialSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason spawnReason, ILivingEntityData entityData, CompoundNBT entityTag) {
        if (!this.isChild()) {
            if (this.rand.nextInt(30) == 0) {
                MobEntity mobEntity = EntityType.ZOMBIFIED_PIGLIN.create(world.getWorld());
                assert mobEntity != null;
                entityData = this.method_30336(world, difficulty, mobEntity, new ZombieEntity.GroupData(ZombieEntity.func_241399_a_(this.rand), false));
                mobEntity.setItemStackToSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.func_230266_a_(null);
            } else if (this.rand.nextInt(10) == 0) {
                AgeableEntity passiveEntity = SSEntities.SOUL_STRIDER.create(world.getWorld());
                assert passiveEntity != null;
                passiveEntity.setGrowingAge(-24000);
                entityData = this.method_30336(world, difficulty, passiveEntity, null);
            } else {
                entityData = new AgeableEntity.AgeableData(0.5F);
            }

        }
        return super.onInitialSpawn(world, difficulty, spawnReason, entityData, entityTag);
    }

    private ILivingEntityData method_30336(IServerWorld serverWorldAccess, DifficultyInstance localDifficulty, MobEntity mobEntity, ILivingEntityData entityData) {
        mobEntity.setPositionAndRotation(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, 0.0F);
        mobEntity.onInitialSpawn(serverWorldAccess, localDifficulty, SpawnReason.JOCKEY, entityData, null);
        mobEntity.startRiding(this, true);
        return new AgeableEntity.AgeableData(0.0F);
    }

    static class Navigation extends GroundPathNavigator {
        Navigation(SoulStriderEntity entity, World world) {
            super(entity, world);
        }

        @Override
        protected PathFinder getPathFinder(int range) {
            this.nodeProcessor = new WalkNodeProcessor();
            return new PathFinder(this.nodeProcessor, range);
        }

        @Override
        protected boolean func_230287_a_(PathNodeType pathType) {
            return pathType != PathNodeType.LAVA && pathType != PathNodeType.DAMAGE_FIRE && pathType != PathNodeType.DANGER_FIRE || super.func_230287_a_(pathType);
        }

        @Override
        public boolean canEntityStandOnPos(BlockPos pos) {
            return this.world.getBlockState(pos).isIn(BlockTags.SOUL_SPEED_BLOCKS) || this.world.getBlockState(pos.down()).isIn(BlockTags.SOUL_SPEED_BLOCKS) || Objects.equals(world.func_242406_i(pos), Optional.of(Biomes.SOUL_SAND_VALLEY)) || super.canEntityStandOnPos(pos);
        }
    }

    @Nullable
    @Override
    public AgeableEntity func_241840_a(ServerWorld p_241840_1_, AgeableEntity p_241840_2_) {
        return null;
    }

    static class LookAtEntityGoal extends Goal {
        protected final MobEntity mob;
        protected Entity target;
        protected final float range;
        private int lookTime;
        protected final float chance;
        protected Class<? extends LivingEntity> targetType;
        protected final EntityPredicate targetPredicate;

        public LookAtEntityGoal(MobEntity mob, float range, float chance) {
            this.mob = mob;
            this.range = range;
            this.chance = chance;
            this.setMutexFlags(EnumSet.of(Goal.Flag.LOOK));
            if (targetType == PlayerEntity.class) {
                this.targetPredicate = new EntityPredicate().setDistance(range).allowFriendlyFire().allowInvulnerable().setSkipAttackChecks().setCustomPredicate(livingEntity -> EntityPredicates.notRiding(mob).test(livingEntity));
            } else {
                this.targetPredicate = new EntityPredicate().setDistance(range).allowFriendlyFire().allowInvulnerable().setSkipAttackChecks();
            }

        }

        @Override
        public boolean shouldExecute() {
            SoulStriderEntity mob = (SoulStriderEntity) this.mob;
            this.targetType = mob.isHiding() ? SoulMothEntity.class : mob.rand.nextInt(5) == 0 ? PlayerEntity.class : SoulStriderEntity.class;

            if (this.mob.getRNG().nextFloat() >= this.chance) {
                return false;
            } else {
                if (this.mob.getAttackTarget() != null) {
                    this.target = this.mob.getAttackTarget();
                }

                if (this.targetType == PlayerEntity.class) {
                    this.target = this.mob.world.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getPosX(),
                                                                  this.mob.getEyeHeight(), this.mob.getPosZ()
                    );
                } else {
                    this.target = this.mob.world.func_225318_b(this.targetType,
                                                               this.targetPredicate, this.mob, this.mob.getPosX(), this.mob.getEyeHeight(), this.mob.getPosZ(),
                                                               this.mob.getBoundingBox().expand(this.range, 3.0D, this.range)
                    );
                }

                return this.target != null;
            }
        }

        @Override
        public boolean shouldContinueExecuting() {
            if (!this.target.isAlive()) {
                return false;
            } else if (this.mob.getDistanceSq(this.target) > (double) (this.range * this.range)) {
                return false;
            } else {
                return this.lookTime > 0;
            }
        }

        @Override
        public void startExecuting() {
            this.lookTime = 40 + this.mob.getRNG().nextInt(40);
        }

        @Override
        public void resetTask() {
            this.target = null;
        }

        @Override
        public void tick() {
            this.mob.getLookController().setLookPosition(this.target.getPosZ(), this.target.getEyeHeight(), this.target.getPosZ());
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
            if (rand.nextInt(40) == 0) {
                eatMoth(this.mob.world.func_225318_b(
                    SoulMothEntity.class,
                    new EntityPredicate().setDistance(range)
                                         .allowFriendlyFire()
                                         .allowInvulnerable()
                                         .setSkipAttackChecks(),
                    this.mob,
                    this.mob.getPosX(), this.mob.getEyeHeight(), this.mob.getPosZ(),
                    this.mob.getBoundingBox().expand(this.range, this.range, this.range)
                ));
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
        public boolean shouldExecute() {
            return mob.isHiding();
        }
    }

    static class EscapePlayersGoal extends PanicGoal {
        public EscapePlayersGoal(CreatureEntity mob, double speed) {
            super(mob, speed);
        }

        @Override
        public boolean shouldExecute() {
            if (creature.world.getClosestPlayer(
                new EntityPredicate()
                    .setDistance(15.0D)
                    .allowFriendlyFire()
                    .allowInvulnerable()
                    .setSkipAttackChecks()
                    .setCustomPredicate(livingEntity -> EntityPredicates.notRiding(creature).test(livingEntity) && EntityPredicates.CAN_AI_TARGET.test(livingEntity)),
                this.creature, this.creature.getPosX(), this.creature.getEyeHeight(), this.creature.getPosZ()
            ) == null && !creature.isBurning()) {
                return false;
            } else {
                if (creature.isBurning()) {
                    BlockPos blockPos = this.getRandPos(creature.world, creature, 5, 4);
                    if (blockPos != null) {
                        this.randPosX = blockPos.getX();
                        this.randPosY = blockPos.getY();
                        this.randPosZ = blockPos.getZ();
                        return true;
                    }
                }

                return this.findRandomPosition();
            }
        }
    }

    @Override
    public void shear(SoundCategory shearedSoundCategory) {
        this.world.playMovingSound(null, this, SoundEvents.ENTITY_SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);

        this.setNoBulbTicks(6000);
        this.setGrowingAge(6000);

        ItemEntity itemEntity = this.entityDropItem(SSBlocks.SOUL_STRIDER_BULB, 1);
        if (itemEntity != null) {
            itemEntity.setMotion(
                itemEntity.getMotion().add(
                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F,
                    this.rand.nextFloat() * 0.05F,
                    (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F
                )
            );
        }
    }

    @Override
    public boolean isShearable() {
        return this.isAlive() && this.hasBulb() && !this.isChild();
    }
}
