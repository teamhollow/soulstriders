package net.teamhollow.soulstriders.entity.soul_strider;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.SegmentedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulStriderEntityModel<T extends SoulStriderEntity> extends SegmentedModel<T> {
    private final ModelRenderer head;
    private final ModelRenderer lure;
    private final ModelRenderer bulb;
    private final ModelRenderer hair;
    private final ModelRenderer left_leg;
    private final ModelRenderer right_leg;

    public SoulStriderEntityModel() {
        textureWidth = 128;
        textureHeight = 128;

        head = new ModelRenderer(this);
        head.setRotationPoint(0.0F, 2.0F, 0.0F);
        head.setTextureOffset(0, 0).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 14.0F, 16.0F, 0.0F, false);

        lure = new ModelRenderer(this);
        lure.setRotationPoint(0.0F, -5.0F, -4.5F);
        head.addChild(lure);
        setRotationAngle(lure, -0.6981F, 0.0F, 0.0F);
        lure.setTextureOffset(0, 15).addBox(0.0F, -6.0F, -13.5F, 0.0F, 10.0F, 15.0F, 0.0F, false);

        bulb = new ModelRenderer(this);
        bulb.setRotationPoint(0.0F, -4.2902F, -1.8391F);
        lure.addChild(bulb);
        setRotationAngle(bulb, 1.0472F, 0.0F, 0.0F);
        bulb.setTextureOffset(0, 0).addBox(-2.0F, -5.9397F, -14.258F, 4.0F, 4.0F, 4.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 22.0F, 0.0F);
        head.addChild(hair);
        hair.setTextureOffset(0, 29).addBox(0.0F, -36.0F, 5.0F, 0.0F, 11.0F, 11.0F, 0.0F, false);

        left_leg = new ModelRenderer(this);
        left_leg.setRotationPoint(-4.0F, 9.0F, 0.0F);
        left_leg.setTextureOffset(30, 29).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F, 0.0F, false);

        right_leg = new ModelRenderer(this);
        right_leg.setRotationPoint(4.0F, 9.0F, 0.0F);
        right_leg.setTextureOffset(30, 30).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F, 0.0F, false);
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        limbSwingAmount = Math.min(0.25F, limbSwingAmount);
        if (entity.getPassengers().size() <= 0) {
            this.head.rotateAngleX = headPitch * 0.017453292F;
            this.head.rotateAngleY = netHeadYaw * 0.017453292F;
        } else {
            this.head.rotateAngleX = 0.0F;
            this.head.rotateAngleY = 0.0F;
        }

        boolean hasBulb = entity.hasBulb() && !entity.isChild();
        bulb.showModel = hasBulb;
        lure.rotateAngleX = hasBulb ? -0.6981F : 0;

        this.head.rotateAngleZ = 0.1F * MathHelper.sin(limbSwing * 1.5F) * 4.0F * limbSwingAmount;
        this.head.rotationPointY = 2.0F;
        this.head.rotationPointY -= 2.0F * MathHelper.cos(limbSwing * 1.5F) * 2.0F * limbSwingAmount;
        this.right_leg.rotateAngleX = MathHelper.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
        this.left_leg.rotateAngleX = MathHelper.sin(limbSwing * 1.5F * 0.5F + 3.1415927F) * 2.0F * limbSwingAmount;
        this.right_leg.rotateAngleZ = 0.17453292F * MathHelper.cos(limbSwing * 1.5F * 0.5F) * limbSwingAmount;
        this.left_leg.rotateAngleZ = 0.17453292F * MathHelper.cos(limbSwing * 1.5F * 0.5F + 3.1415927F) * limbSwingAmount;
        this.right_leg.rotationPointY = 8.0F + 2.0F * MathHelper.sin(limbSwing * 1.5F * 0.5F + 3.1415927F) * 2.0F * limbSwingAmount;
        this.left_leg.rotationPointY = 8.0F + 2.0F * MathHelper.sin(limbSwing * 1.5F * 0.5F) * 2.0F * limbSwingAmount;
    }

    public void setRotationAngle(ModelRenderer bone, float x, float y, float z) {
        bone.rotateAngleX = x;
        bone.rotateAngleY = y;
        bone.rotateAngleZ = z;
    }

    @Override
    public Iterable<ModelRenderer> getParts() {
        return ImmutableList.of(head, left_leg, right_leg);
    }
}
