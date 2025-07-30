package dev.tenacity.utils.client.addons.mobends.animation.player;

import dev.tenacity.utils.client.addons.mobends.client.model.ModelRendererBends;
import dev.tenacity.utils.client.addons.mobends.data.Data_Player;
import net.minecraft.entity.player.EntityPlayer;
import dev.tenacity.utils.client.addons.mobends.client.model.entity.ModelBendsPlayer;

public class Animation_Attack_PunchStance {

    public static void animate(EntityPlayer player, ModelBendsPlayer model, Data_Player data) {
        if (!(data.motion.x == 0 & data.motion.z == 0)) {
            return;
        }

        model.renderRotation.setSmoothY(20.0f);
        model.renderOffset.setSmoothY(-2.0f);

        ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothX(-90, 0.3f);
        model.bipedRightForeArm.rotation.setSmoothX(-80, 0.3f);

        ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothX(-90, 0.3f);
        model.bipedLeftForeArm.rotation.setSmoothX(-80, 0.3f);

        ((ModelRendererBends) model.bipedRightArm).rotation.setSmoothZ(20, 0.3f);
        ((ModelRendererBends) model.bipedLeftArm).rotation.setSmoothZ(-20, 0.3f);

        ((ModelRendererBends) model.bipedBody).rotation.setSmoothX(10, 0.3f);

        ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothX(-30, 0.3f);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothX(-30, 0.3f);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothY(-25, 0.3f);
        ((ModelRendererBends) model.bipedRightLeg).rotation.setSmoothZ(10);
        ((ModelRendererBends) model.bipedLeftLeg).rotation.setSmoothZ(-10);

        model.bipedRightForeLeg.rotation.setSmoothX(30, 0.3f);
        model.bipedLeftForeLeg.rotation.setSmoothX(30, 0.3f);

        ((ModelRendererBends) model.bipedHead).rotation.setY(model.headRotationY - 20);
        ((ModelRendererBends) model.bipedHead).rotation.setX(model.headRotationX - 10);
    }
}
