package net.teamhollow.soulstriders.entity.soul_moth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.init.SSEntities;

@Environment(EnvType.CLIENT)
public class SoulMothEntityRenderer extends MobEntityRenderer<SoulMothEntity, SoulMothEntityModel<SoulMothEntity>> {
    private static final Identifier TEXTURE = SSEntities.texture("soul_moth/soul_moth");

    public SoulMothEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SoulMothEntityModel<SoulMothEntity>(), 0.01F);
    }

    @Override
    public Identifier getTexture(SoulMothEntity entity) {
        return TEXTURE;
    }
}
