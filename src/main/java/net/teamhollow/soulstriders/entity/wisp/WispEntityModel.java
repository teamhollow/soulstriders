package net.teamhollow.soulstriders.entity.wisp;

import net.minecraft.util.Identifier;
import net.teamhollow.soulstriders.SoulStriders;
import software.bernie.geckolib.animation.model.AnimatedEntityModel;
import software.bernie.geckolib.animation.render.AnimatedModelRenderer;

public class WispEntityModel extends AnimatedEntityModel<WispEntity> {
    private final AnimatedModelRenderer wisp;

    public WispEntityModel() {
        textureWidth = 48;
        textureHeight = 48;

        wisp = new AnimatedModelRenderer(this);
        wisp.setRotationPoint(0.5F, 20.5F, 0.5F);
        wisp.setTextureOffset(0, 0).addBox(-3.5F, -3.5F, -6.5F, 7.0F, 7.0F, 13.0F, 0.0F, false);
        wisp.setModelRendererName("Wisp");
        this.registerModelRenderer(wisp);

        this.rootBones.add(wisp);
    }

    @Override
    public Identifier getAnimationFileLocation() {
        return new Identifier(SoulStriders.MOD_ID, "animations/wisp.json");
    }
}
