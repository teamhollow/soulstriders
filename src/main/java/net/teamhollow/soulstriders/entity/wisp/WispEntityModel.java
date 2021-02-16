package net.teamhollow.soulstriders.entity.wisp;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CompositeEntityModel;

@Environment(EnvType.CLIENT)
public class WispEntityModel<T extends WispEntity> extends CompositeEntityModel<T> {
    private final ModelPart wisp;

    public WispEntityModel() {
        textureWidth = 48;
        textureHeight = 48;

        wisp = new ModelPart(this);
        wisp.setPivot(-1.5F, 20.5F, 6.5F);
        wisp.setTextureOffset(0, 0).addCuboid(-1.5F, -3.5F, -12.5F, 7.0F, 7.0F, 13.0F, 0.0F, false);
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(wisp);
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {}
}
