package net.teamhollow.soulstriders.entity.wisp;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.teamhollow.soulstriders.SoulStriders;

@OnlyIn(Dist.CLIENT)
public class WispEntityRenderer extends MobRenderer<WispEntity, WispEntityModel<WispEntity>> {
    private static final ResourceLocation TEXTURE = SoulStriders.id("textures/entity/wisp/wisp.png");

    public WispEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new WispEntityModel<>(), 0.25F);
    }

    @Override
    protected int getBlockLight(WispEntity entity, BlockPos blockPos) {
        return 15;
    }

    @Override
    public ResourceLocation getEntityTexture(WispEntity entity) {
        return TEXTURE;
    }
}
