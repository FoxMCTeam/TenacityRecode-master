package dev.tenacity.utils.client.addons.mobends.pack;

import dev.tenacity.utils.client.addons.mobends.client.model.ModelRendererBends;
import dev.tenacity.utils.client.addons.mobends.client.model.entity.ModelBendsPlayer;

import java.util.ArrayList;
import java.util.List;

public class BendsPack {

    public String filename;
    public String displayName;
    public String author;
    public String description;
    public static List<BendsTarget> targets = new ArrayList<>();

    public static BendsTarget getTargetByID(String argID) {
        for (BendsTarget target : targets) {
            if (target.mob.equalsIgnoreCase(argID)) {
                return target;
            }
        }
        return null;
    }

    public static void animate(ModelBendsPlayer model, String target, String anim) {
        BendsTarget bendsTarget = getTargetByID(target);
        if (bendsTarget == null) {
            return;
        }

        bendsTarget.applyToModel((ModelRendererBends) model.bipedBody, anim, "body");
        bendsTarget.applyToModel((ModelRendererBends) model.bipedHead, anim, "head");
        bendsTarget.applyToModel((ModelRendererBends) model.bipedLeftArm, anim, "leftArm");
        bendsTarget.applyToModel((ModelRendererBends) model.bipedRightArm, anim, "rightArm");
        bendsTarget.applyToModel((ModelRendererBends) model.bipedLeftLeg, anim, "leftLeg");
        bendsTarget.applyToModel((ModelRendererBends) model.bipedRightLeg, anim, "rightLeg");
        bendsTarget.applyToModel(model.bipedLeftForeArm, anim, "leftForeArm");
        bendsTarget.applyToModel(model.bipedRightForeArm, anim, "rightForeArm");
        bendsTarget.applyToModel(model.bipedLeftForeLeg, anim, "leftForeLeg");
        bendsTarget.applyToModel(model.bipedRightForeLeg, anim, "rightForeLeg");

        bendsTarget.applyToModel(model.renderItemRotation, anim, "itemRotation");
        bendsTarget.applyToModel(model.renderRotation, anim, "playerRotation");
    }
}
