package net.teamhollow.soulstriders.entity.wisp;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WispEntityModel<T extends WispEntity> extends SegmentedModel<T> {
    private final ModelRenderer wisp;

    public WispEntityModel() {
        textureWidth = 48;
        textureHeight = 48;

        wisp = new ModelRenderer(this);
        wisp.setRotationPoint(-1.5F, 20.5F, 6.5F);
        wisp.setTextureOffset(0, 0).addBox(-1.5F, -3.5F, -12.5F, 7.0F, 7.0F, 13.0F, 0.0F, false);
    }

    @Override
    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(wisp);
    }

    @Override
    public void setRotationAngles(T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }
}
