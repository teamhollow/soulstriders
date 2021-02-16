package net.teamhollow.soulstriders.entity.soul_moth;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.teamhollow.soulstriders.SoulStriders;

@OnlyIn(Dist.CLIENT)
public class SoulMothEntityRenderer extends MobRenderer<SoulMothEntity, SoulMothEntityModel<SoulMothEntity>> {
    private static final ResourceLocation TEXTURE = SoulStriders.id("textures/entity/soul_moth/soul_moth.png");

    public SoulMothEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SoulMothEntityModel<>(), 0);
    }

    @Override
    public ResourceLocation getEntityTexture(SoulMothEntity entity) {
        return TEXTURE;
    }

    @Override
    protected int getBlockLight(SoulMothEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    protected void preRenderCallback(SoulMothEntity entity, MatrixStack matrixStack, float amount) {
        super.preRenderCallback(entity, matrixStack, amount);
        matrixStack.scale(0.5F, 0.5F, 0.5F);
    }
}
