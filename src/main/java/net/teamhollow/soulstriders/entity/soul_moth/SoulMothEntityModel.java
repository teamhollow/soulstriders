package net.teamhollow.soulstriders.entity.soul_moth;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class SoulMothEntityModel<T extends SoulMothEntity> extends SegmentedModel<T> {
    private final ModelRenderer soulMoth;

    public SoulMothEntityModel() {
        textureWidth = 4;
        textureHeight = 4;

        soulMoth = new ModelRenderer(this);
        soulMoth.setRotationPoint(0.0F, 24.0F, 0.0F);
        soulMoth.setTextureOffset(0, 0).addBox(-1.0F, -2F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(soulMoth);
    }
}
