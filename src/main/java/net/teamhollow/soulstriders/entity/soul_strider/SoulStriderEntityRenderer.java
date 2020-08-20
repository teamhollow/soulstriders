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
    private static final Identifier TEXTURE = SSEntities.texture("soul_strider/soul_strider");
    private static final Identifier WARM_TEXTURE = SSEntities.texture("strider/soul_strider_warm");

    public SoulStriderEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SoulStriderEntityModel<SoulStriderEntity>(), 0.5F);
        this.addFeature(
            new SaddleFeatureRenderer<SoulStriderEntity, SoulStriderEntityModel<SoulStriderEntity>>(
                this,
                new SoulStriderEntityModel<SoulStriderEntity>(),
                new Identifier("textures/entity/strider/strider_saddle.png")
            )
        );
    }

    @Override
    public Identifier getTexture(SoulStriderEntity soulStriderEntity) {
        return !soulStriderEntity.isCold() ? WARM_TEXTURE : TEXTURE;
    }

    @Override
    protected void scale(SoulStriderEntity soulStriderEntity, MatrixStack matrixStack, float amount) {
        float scale = 0.9375F;
        if (soulStriderEntity.isBaby()) {
            scale *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        matrixStack.scale(scale, scale, scale);
    }
}
