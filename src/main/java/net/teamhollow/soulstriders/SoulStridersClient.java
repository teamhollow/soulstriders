package net.teamhollow.soulstriders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.teamhollow.soulstriders.entity.soul_moth.SoulMothEntityRenderer;
import net.teamhollow.soulstriders.entity.soul_strider.SoulStriderEntityRenderer;
import net.teamhollow.soulstriders.entity.wisp.WispEntityRenderer;
import net.teamhollow.soulstriders.init.SSEntities;

public class SoulStridersClient extends SoulStriders {
    @Override
    public void init() {
        super.init();
        EntityRendererManager rendererManager = Minecraft.getInstance().getRenderManager();
        rendererManager.register(SSEntities.WISP, new WispEntityRenderer(rendererManager));
        rendererManager.register(SSEntities.SOUL_MOTH, new SoulMothEntityRenderer(rendererManager));
        rendererManager.register(SSEntities.SOUL_STRIDER, new SoulStriderEntityRenderer(rendererManager));
    }
}
