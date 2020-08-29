package net.teamhollow.soulstriders.entity.soul_moth;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

public class SoulMothEntityModel<T extends SoulMothEntity> extends CompositeEntityModel<T> {
    private final ModelPart soulMoth;

    public SoulMothEntityModel() {
        textureWidth = 4;
        textureHeight = 4;

        soulMoth = new ModelPart(this);
        soulMoth.setPivot(0.0F, 24.0F, 0.0F);
        soulMoth.setTextureOffset(0, 0).addCuboid(-1.0F, -2F, -1.0F, 2.0F, 2.0F, 2.0F, 0.0F, false);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(soulMoth);
    }
}
