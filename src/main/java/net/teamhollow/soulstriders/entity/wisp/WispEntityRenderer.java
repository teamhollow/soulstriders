package net.teamhollow.soulstriders.entity.wisp;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.init.SSEntities;

@Environment(EnvType.CLIENT)
public class WispEntityRenderer extends MobEntityRenderer<WispEntity, WispEntityModel<WispEntity>> {
    private static final Identifier TEXTURE = SSEntities.texture("wisp/wisp");

    public WispEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new WispEntityModel<WispEntity>(), 0.25F);
    }

    @Override
    public Identifier getTexture(WispEntity entity) {
        return TEXTURE;
    }
}
