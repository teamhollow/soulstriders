package net.teamhollow.soulstriders.entity.soul_moth;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.init.SSEntities;

@Environment(EnvType.CLIENT)
public class SoulMothEntityRenderer extends MobEntityRenderer<SoulMothEntity, SoulMothEntityModel<SoulMothEntity>> {
    private static final Identifier TEXTURE = SSEntities.texture("soul_moth/soul_moth");

    public SoulMothEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SoulMothEntityModel<SoulMothEntity>(), 0);
    }

    @Override
    public Identifier getTexture(SoulMothEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(SoulMothEntity entity, MatrixStack matrixStack, float amount) {
        super.scale(entity, matrixStack, amount);
        matrixStack.scale(0.5F, 0.5F, 0.5F);
    }
}
