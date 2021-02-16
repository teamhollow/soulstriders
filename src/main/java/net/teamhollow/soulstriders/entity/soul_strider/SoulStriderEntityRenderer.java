package net.teamhollow.soulstriders.entity.soul_strider;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.init.SSEntities;

@Environment(EnvType.CLIENT)
public class SoulStriderEntityRenderer extends MobEntityRenderer<SoulStriderEntity, SoulStriderEntityModel<SoulStriderEntity>> {
    public SoulStriderEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SoulStriderEntityModel<>(), 0.5F);
        this.addFeature(new SaddleFeatureRenderer<>(this, new SoulStriderEntityModel<>(), SSEntities.texture("soul_strider/soul_strider_saddle")));
    }

    @Override
    public Identifier getTexture(SoulStriderEntity entity) {
        return !entity.isSoulSurrounded() ? SSEntities.texture("soul_strider/soul_strider_soulless") : SSEntities.texture("soul_strider/soul_strider");
    }

    @Override
    protected void scale(SoulStriderEntity entity, MatrixStack matrixStack, float amount) {
        float scale = 0.9375F;
        if (entity.isBaby()) {
            scale *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        matrixStack.scale(scale, scale, scale);
    }

    @Override
    protected void setupTransforms(SoulStriderEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        if (entity.isHiding()) matrices.translate(0.0D, -1.08D, 0.0D);
        super.setupTransforms(entity, matrices, animationProgress, bodyYaw, tickDelta);
    }
}
