package net.teamhollow.soulstriders.entity.soul_strider;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.teamhollow.soulstriders.SoulStriders;

@OnlyIn(Dist.CLIENT)
public class SoulStriderEntityRenderer extends MobRenderer<SoulStriderEntity, SoulStriderEntityModel<SoulStriderEntity>> {
    public SoulStriderEntityRenderer(EntityRendererManager entityRenderDispatcher) {
        super(entityRenderDispatcher, new SoulStriderEntityModel<>(), 0.5F);
        this.addLayer(new SaddleLayer<>(this, new SoulStriderEntityModel<>(), SoulStriders.id("textures/entity/soul_strider/soul_strider_saddle.png")));
    }

    @Override
    public ResourceLocation getEntityTexture(SoulStriderEntity entity) {
        return !entity.isSoulSurrounded()
               ? SoulStriders.id("textures/entity/soul_strider/soul_strider_soulless.png")
               : SoulStriders.id("textures/entity/soul_strider/soul_strider.png");
    }

    @Override
    protected void preRenderCallback(SoulStriderEntity entity, MatrixStack matrixStack, float amount) {
        float scale = 0.9375F;
        if (entity.isChild()) {
            scale *= 0.5F;
            this.shadowSize = 0.25F;
        } else {
            this.shadowSize = 0.5F;
        }

        matrixStack.scale(scale, scale, scale);
    }

    @Override
    protected void applyRotations(SoulStriderEntity entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta) {
        if (entity.isHiding()) matrices.translate(0.0D, -1.08D, 0.0D);
        super.applyRotations(entity, matrices, animationProgress, bodyYaw, tickDelta);
    }
}
