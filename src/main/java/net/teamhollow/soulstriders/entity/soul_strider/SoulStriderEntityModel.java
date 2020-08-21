package net.teamhollow.soulstriders.entity.soul_strider;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class SoulStriderEntityModel<T extends SoulStriderEntity> extends CompositeEntityModel<T> {
    private final ModelPart head;
    private final ModelPart lure;
    private final ModelPart bulb;
    private final ModelPart hair;
    private final ModelPart left_leg;
    private final ModelPart right_leg;

    public SoulStriderEntityModel() {
        textureWidth = 128;
        textureHeight = 128;

        head = new ModelPart(this);
        head.setPivot(0.0F, 2.0F, 0.0F);
        head.setTextureOffset(0, 0).addCuboid(-8.0F, -7.0F, -8.0F, 16.0F, 14.0F, 16.0F, 0.0F, false);

        lure = new ModelPart(this);
        lure.setPivot(0.0F, -5.0F, -4.5F);
        head.addChild(lure);
        setRotationAngle(lure, -0.6981F, 0.0F, 0.0F);
        lure.setTextureOffset(0, 15).addCuboid(0.0F, -6.0F, -13.5F, 0.0F, 10.0F, 15.0F, 0.0F, false);

        bulb = new ModelPart(this);
        bulb.setPivot(0.0F, -4.2902F, -1.8391F);
        lure.addChild(bulb);
        setRotationAngle(bulb, 1.0472F, 0.0F, 0.0F);
        bulb.setTextureOffset(0, 0).addCuboid(-2.0F, -5.9397F, -14.258F, 4.0F, 4.0F, 4.0F, 0.0F, false);

        hair = new ModelPart(this);
        hair.setPivot(0.0F, 22.0F, 0.0F);
        head.addChild(hair);
        hair.setTextureOffset(0, 29).addCuboid(0.0F, -36.0F, 5.0F, 0.0F, 11.0F, 11.0F, 0.0F, false);

        left_leg = new ModelPart(this);
        left_leg.setPivot(-4.0F, 9.0F, 0.0F);
        left_leg.setTextureOffset(30, 29).addCuboid(-2.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F, 0.0F, false);

        right_leg = new ModelPart(this);
        right_leg.setPivot(4.0F, 9.0F, 0.0F);
        right_leg.setTextureOffset(30, 30).addCuboid(-2.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F, 0.0F, false);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        limbSwingAmount = Math.min(0.25F, limbSwingAmount);
        if (entity.getPassengerList().size() <= 0) {
            this.head.pitch = headPitch * 0.017453292F;
            this.head.yaw = netHeadYaw * 0.017453292F;
        } else {
            this.head.pitch = 0.0F;
            this.head.yaw = 0.0F;
        }

        this.head.roll = 0.1F * MathHelper.sin(limbSwing * 1.5F) * 4.0F * limbSwingAmount;
        this.head.pivotY = 2.0F;
        this.head.pivotY -= 2.0F * MathHelper.cos(limbSwing * 1.5F) * 2.0F * limbSwingAmount;
        this.right_leg.pitch = MathHelper.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
        this.left_leg.pitch = MathHelper.sin(limbSwing * 1.5F * 0.5F + 3.1415927F) * 2.0F * limbSwingAmount;
        this.right_leg.roll = 0.17453292F * MathHelper.cos(limbSwing * 1.5F * 0.5F) * limbSwingAmount;
        this.left_leg.roll = 0.17453292F * MathHelper.cos(limbSwing * 1.5F * 0.5F + 3.1415927F) * limbSwingAmount;
        this.right_leg.pivotY = 8.0F + 2.0F * MathHelper.sin(limbSwing * 1.5F * 0.5F + 3.1415927F) * 2.0F * limbSwingAmount;
        this.left_leg.pivotY = 8.0F + 2.0F * MathHelper.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
    }

    public void setRotationAngle(ModelPart bone, float x, float y, float z) {
        bone.pitch = x;
        bone.yaw = y;
        bone.roll = z;
    }

    @Override
    public Iterable<ModelPart> getParts() {
        return ImmutableList.of(head, left_leg, right_leg);
    }
}
