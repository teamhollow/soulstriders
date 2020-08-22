package net.teamhollow.soulstriders.init;

import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.teamhollow.soulstriders.SoulStriders;
import net.teamhollow.soulstriders.entity.soul_moth.*;
import net.teamhollow.soulstriders.entity.soul_strider.*;
import net.teamhollow.soulstriders.entity.wisp.*;

public class SSEntities {
    public static final EntityType<SoulStriderEntity> SOUL_STRIDER = register(
        SoulStriderEntity.id,
        SoulStriderEntity.builder,
        SoulStriderEntity.spawnEggColors
    );
    public static final EntityType<WispEntity> WISP = register(
        WispEntity.id,
        WispEntity.builder,
        WispEntity.spawnEggColors
    );
    public static final EntityType<SoulMothEntity> SOUL_MOTH = register(
        SoulMothEntity.id,
        SoulMothEntity.builder,
        SoulMothEntity.spawnEggColors
    );

    public SSEntities() {
        registerDefaultAttributes(SOUL_STRIDER, SoulStriderEntity.createStriderAttributes());
        registerDefaultAttributes(WISP, WispEntity.createWispAttributes());
        registerDefaultAttributes(SOUL_MOTH, SoulMothEntity.createSoulMothAttributes());
    }

    public static void registerRenderers() {
        EntityRendererRegistry INSTANCE = EntityRendererRegistry.INSTANCE;

        INSTANCE.register(
            SOUL_STRIDER,
            (entityRenderDispatcher, context) -> new SoulStriderEntityRenderer(entityRenderDispatcher)
        );
        INSTANCE.register(
            WISP,
            (entityRenderDispatcher, context) -> new WispEntityRenderer(entityRenderDispatcher)
        );
        INSTANCE.register(
            SOUL_MOTH,
            (entityRenderDispatcher, context) -> new SoulMothEntityRenderer(entityRenderDispatcher)
        );
    }

    private static <T extends Entity> EntityType<T> register(String id, EntityType.Builder<T> entityType,
            int[] spawnEggColors) {
        EntityType<T> builtEntityType = entityType.build(id);

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
