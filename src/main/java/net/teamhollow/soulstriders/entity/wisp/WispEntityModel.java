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
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // previously the render function, render code was moved to a method below
    }

    public void setRotationAngle(ModelPart bone, float x, float y, float z) {
        bone.pitch = x;
        bone.yaw = y;
        bone.roll = z;
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(wisp);
    }
}
