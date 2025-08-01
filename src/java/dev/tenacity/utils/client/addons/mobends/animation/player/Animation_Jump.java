package dev.tenacity.utils.client.addons.mobends.animation.player;

import dev.tenacity.utils.client.addons.mobends.client.model.ModelRendererBends;
import dev.tenacity.utils.client.addons.mobends.data.Data_Player;
import dev.tenacity.utils.client.addons.mobends.data.EntityData;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import dev.tenacity.utils.client.addons.mobends.animation.Animation;
import dev.tenacity.utils.client.addons.mobends.client.model.entity.ModelBendsPlayer;

public class Animation_Jump extends Animation {


    public String getName() {
        return "jump";
    }

    @Override
    public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
        ModelBendsPlayer model = (ModelBendsPlayer) argModel;
        Data_Player data = (Data_Player) argData;

        ((ModelRendererBends) model.bipedBody).rotation.setSmoothX(0, 0.3f);
        ((ModelRendererBends) model.bipedBody).rotation.setSmoothY(0, 0.1f);
        ((ModelRendererBends) model.bipedBody).rotation.setSmoothZ(0.0f, 0.3f);
        ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothZ(45, 0.05f);
        ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothZ(-45, 0.05f);
        ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(0.0f, 0.5f);
        ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothX(0.0f, 0.5f);
        ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothZ(10, 0.1f);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothZ(-10, 0.1f);
        ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-20, 0.1f);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-20, 0.1f);

        ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-45, 0.1f);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-45, 0.1f);
        model.bipedRightForeLeg.rotation.setSmoothX(50, 0.1f);
        model.bipedLeftForeLeg.rotation.setSmoothX(50, 0.1f);

        model.bipedRightForeArm.rotation.setSmoothX(0, 0.3f);
        model.bipedLeftForeArm.rotation.setSmoothX(0, 0.3f);

        ((ModelRendererBends) model.bipedHead).rotation.setY(model.headRotationY);
        ((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX - model.bipedBody.rotateAngleX);

        if (data.ticksAfterLiftoff < 2.0f) {
            ((ModelRendererBends) model.bipedBody).rotation.setSmoothX(20.0f, 2f);
            ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(0.0f, 2f);
            ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(0.0f, 2f);
            model.bipedRightForeLeg.rotation.setSmoothX(0.0f, 2f);
            model.bipedLeftForeLeg.rotation.setSmoothX(0.0f, 2f);
            ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothZ(2, 2f);
            ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothZ(-2, 2f);
            model.bipedRightForeArm.rotation.setSmoothX(-20, 2f);
            model.bipedLeftForeArm.rotation.setSmoothX(-20, 2f);
        }

        if (argData.motion.x != 0 | argData.motion.z != 0) {
            if (argEntity.isSprinting()) {
                float bodyRot = 0.0f;
                float bodyLean = argData.motion.y;
                if (bodyLean < -0.2f) bodyLean = -0.2f;
                if (bodyLean > 0.2f) bodyLean = 0.2f;
                bodyLean = bodyLean * -100.0f + 20;

                ((ModelRendererBends) model.bipedBody).rotation.setSmoothX(bodyLean, 0.3f);
                ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothZ(5, 0.3f);
                ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothZ(-5, 0.3f);
                ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothZ(10, 0.3f);
                ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothZ(-10, 0.3f);

                if (data.sprintJumpLeg) {
                    ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-45, 0.4f);
                    ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(45, 0.4f);
                    ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(50, 0.3f);
                    ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothX(-50, 0.3f);
                    bodyRot = 20;
                } else {
                    ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(45, 0.4f);
                    ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-45, 0.4f);
                    ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(-50, 0.3f);
                    ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothX(50, 0.3f);
                    bodyRot = -20;
                }

                ((ModelRendererBends) model.bipedBody).rotation.setSmoothY(bodyRot, 0.3f);

                ((ModelRendererBends) model.bipedHead).rotation.setY(model.headRotationY - bodyRot);
                ((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX - 20);

                float var = model.bipedRightLeg.rotateAngleX;
                model.bipedLeftForeLeg.rotation.setSmoothX((var < 0 ? 45 : 2), 0.3f);
                model.bipedRightForeLeg.rotation.setSmoothX((var < 0 ? 2 : 45), 0.3f);
            } else {
                ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-5.0f + 0.5f * (float) ((MathHelper.cos(model.armSwing * 0.6662F) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f), 1.0f);
                ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-5.0f + 0.5f * (float) ((MathHelper.cos(model.armSwing * 0.6662F + (float) Math.PI) * 1.4F * model.armSwingAmount) / Math.PI * 180.0f), 1.0f);

                float var = (float) ((model.armSwing * 0.6662F) / Math.PI) % 2;
                model.bipedLeftForeLeg.rotation.setSmoothX((var > 1 ? 45 : 0), 0.3f);
                model.bipedRightForeLeg.rotation.setSmoothX((var > 1 ? 0 : 45), 0.3f);
                model.bipedLeftForeArm.rotation.setSmoothX(((float) (Math.cos(model.armSwing * 0.6662F + Math.PI / 2) + 1.0f) / 2.0f) * -20, 1.0f);
                model.bipedRightForeArm.rotation.setSmoothX(((float) (Math.cos(model.armSwing * 0.6662F) + 1.0f) / 2.0f) * -20, 0.3f);
            }
        }
    }
}
