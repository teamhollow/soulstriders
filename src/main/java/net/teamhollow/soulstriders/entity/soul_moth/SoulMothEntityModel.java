package net.teamhollow.soulstriders.entity.soul_moth;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

@Environment(EnvType.CLIENT)
public class SoulMothEntityModel<T extends SoulMothEntity> extends CompositeEntityModel<T> {
    private final ModelPart soulMoth;

    public SoulMothEntityModel() {
        textureWidth = 4;
        textureHeight = 4;

        soulMoth = new ModelPart(this);
        soulMoth.setPivot(0.0F, 24.0F, 0.0F);
        soulMoth.setTextureOffset(0, 0).addCuboid(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, 0.0F, false);
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(soulMoth);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}
}
