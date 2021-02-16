package net.teamhollow.soulstriders.init;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntity;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntity;
import net.teamhollow.soulstriders.entity.wisp.WispEntity;

@Mod.EventBusSubscriber(modid = SoulStriders.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SSEntities {
    public static final EntityType<SoulStriderEntity> SOUL_STRIDER = new EntityType<>(
        SoulStriderEntity::new,
        EntityClassification.CREATURE,
        true,
        true,
        true,
        false,
        ImmutableSet.of(),
        EntitySize.flexible(.9f, 1.7f),
        10, 3
    );
    public static final EntityType<WispEntity> WISP = new EntityType<>(
        WispEntity::new,
        EntityClassification.AMBIENT,
        true,
        true,
        true,
        false,
        ImmutableSet.of(),
        EntitySize.fixed(.5f, .5f),
        5, 3
    );
    public static final EntityType<SoulMothEntity> SOUL_MOTH = new EntityType<>(
        SoulMothEntity::new,
        EntityClassification.AMBIENT,
        true,
        true,
        true,
        false,
        ImmutableSet.of(),
        EntitySize.fixed(.07f, .07f),
        5, 3
    );

    public static void register(IForgeRegistry<EntityType<?>> registry) {
        registry.registerAll(
            SOUL_STRIDER.setRegistryName(SoulStriders.id(SoulStriderEntity.id)),
            WISP.setRegistryName(SoulStriders.id(WispEntity.id)),
            SOUL_MOTH.setRegistryName(SoulStriders.id(SoulMothEntity.id))
        );
    }

    public static void init() {
        EntitySpawnPlacementRegistry.register(
            SOUL_MOTH,
            EntitySpawnPlacementRegistry.PlacementType.NO_RESTRICTIONS,
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            SoulMothEntity::canSpawnOn
        );
        EntitySpawnPlacementRegistry.register(
            SOUL_STRIDER,
            EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
            Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
            SoulStriderEntity::canSpawn
        );
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(
            WISP,
            MobEntity.func_233666_p_()
                     .createMutableAttribute(Attributes.MAX_HEALTH, 6)
                     .create()
        );
        event.put(
            SOUL_MOTH,
            MobEntity.func_233666_p_()
                     .createMutableAttribute(Attributes.MAX_HEALTH, 1)
                     .create()
        );
        event.put(
            SOUL_STRIDER,
            MobEntity.func_233666_p_()
                     .createMutableAttribute(Attributes.MOVEMENT_SPEED, .175)
                     .createMutableAttribute(Attributes.FOLLOW_RANGE, 16.)
                     .create()
        );
    }
}
