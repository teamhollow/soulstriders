package net.teamhollow.soulstriders.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Heightmap;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.entity.soul_moth.*;
import net.teamhollow.soulstriders.entity.soul_strider.*;
import net.teamhollow.soulstriders.entity.wisp.*;

public class SSEntities {
    public static final EntityType<SoulStriderEntity> SOUL_STRIDER = register(
        SoulStriderEntity.id,
        FabricEntityTypeBuilder
            .<SoulStriderEntity>createMob()
            .entityFactory(SoulStriderEntity::new)
            .spawnGroup(SpawnGroup.CREATURE)
            .dimensions(EntityDimensions.changing(0.9F, 1.7F))
            .trackRangeBlocks(10)
            .defaultAttributes(() -> MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.17499999701976776D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0D))
            .spawnRestriction(SpawnRestriction.Location.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SoulStriderEntity::canSpawn),
        SoulStriderEntity.spawnEggColors
    );
    public static final EntityType<WispEntity> WISP = register(
        WispEntity.id,
        FabricEntityTypeBuilder
            .<WispEntity>createMob()
            .entityFactory(WispEntity::new)
            .spawnGroup(SpawnGroup.AMBIENT)
            .fireImmune()
            .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
            .trackRangeBlocks(5)
            .defaultAttributes(() -> MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0D)),
        WispEntity.spawnEggColors
    );
    public static final EntityType<SoulMothEntity> SOUL_MOTH = register(
        SoulMothEntity.id,
        FabricEntityTypeBuilder
            .<SoulMothEntity>createMob()
            .entityFactory(SoulMothEntity::new)
            .spawnGroup(SpawnGroup.AMBIENT)
            .dimensions(EntityDimensions.fixed(0.07F, 0.07F))
            .trackRangeBlocks(5)
            .defaultAttributes(() -> MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0D))
            .spawnRestriction(SpawnRestriction.Location.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SoulMothEntity::canMobSpawn),
        SoulMothEntity.spawnEggColors
    );

    public SSEntities() {}

    private static <T extends Entity> EntityType<T> register(String id, FabricEntityTypeBuilder<T> entityType, int[] spawnEggColors) {
        EntityType<T> builtEntityType = entityType.build();

        if (spawnEggColors[0] != 0)
            SSItems.register(id + "_spawn_egg", new SpawnEggItem(builtEntityType, spawnEggColors[0], spawnEggColors[1],
                    new Item.Settings().maxCount(64).group(SoulStriders.ITEM_GROUP)));

        return Registry.register(Registry.ENTITY_TYPE, new Identifier(SoulStriders.MOD_ID, id), builtEntityType);
    }

    // private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> entityType) {
    //     return register(id, entityType, new int[] { 0, 0 });
    // }

    public static void registerDefaultAttributes(EntityType<? extends LivingEntity> type,
            DefaultAttributeContainer.Builder builder) {
        FabricDefaultAttributeRegistry.register(type, builder);
    }

    public static Identifier texture(String path) {
        return SoulStriders.texture("entity/" + path);
    }
}
