package net.teamhollow.soulstriders;

import static net.teamhollow.soulstriders.SoulStriders.log;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntityRenderer;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntityRenderer;
import net.teamhollow.soulstriders.entity.wisp.WispEntityRenderer;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulStridersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        log("Initializing client");

        this.registerRenderers();

        log("Initialized client");
    }

    public void registerRenderers() {
        EntityRendererRegistry INSTANCE = EntityRendererRegistry.INSTANCE;

        INSTANCE.register(
            SSEntities.SOUL_STRIDER,
            (entityRenderDispatcher, context) -> new SoulStriderEntityRenderer(entityRenderDispatcher)
        );
        INSTANCE.register(
            SSEntities.WISP,
            (entityRenderDispatcher, context) -> new WispEntityRenderer(entityRenderDispatcher)
        );
        INSTANCE.register(
            SSEntities.SOUL_MOTH,
            (entityRenderDispatcher, context) -> new SoulMothEntityRenderer(entityRenderDispatcher)
        );
    }
}
