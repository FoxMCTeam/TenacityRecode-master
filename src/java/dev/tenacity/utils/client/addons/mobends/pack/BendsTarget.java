package dev.tenacity.utils.client.addons.mobends.pack;

import dev.tenacity.utils.client.addons.mobends.client.model.ModelRendererBends;
import dev.tenacity.utils.client.addons.mobends.util.EnumAxis;
import dev.tenacity.utils.client.addons.mobends.util.SmoothVector3f;

import java.util.ArrayList;
import java.util.List;

public class BendsTarget {
    public String mob;
    public List<BendsAction> actions = new ArrayList<BendsAction>();
    public float visual_DeletePopUp;

    public BendsTarget(String argMob) {
        this.mob = argMob;
        this.visual_DeletePopUp = 0;
    }

    public void applyToModel(ModelRendererBends box, String anim, String model) {
        for (BendsAction action : actions) {
            if ((action.anim.equalsIgnoreCase(anim) | action.anim.equalsIgnoreCase("all")) &
                    action.model.equalsIgnoreCase(model)) {
                if (action.prop == BendsAction.EnumBoxProperty.ROT) {
                    box.rotation.setSmooth(action.axis, action.getNumber((action.axis == EnumAxis.X ? box.rotation.vFinal.x : action.axis == EnumAxis.Y ? box.rotation.vFinal.y : box.rotation.vFinal.z)), action.smooth);
                } else if (action.prop == BendsAction.EnumBoxProperty.PREROT) {
                    box.pre_rotation.setSmooth(action.axis, action.getNumber((action.axis == EnumAxis.X ? box.pre_rotation.vFinal.x : action.axis == EnumAxis.Y ? box.pre_rotation.vFinal.y : box.pre_rotation.vFinal.z)), action.smooth);
                } else if (action.prop == BendsAction.EnumBoxProperty.SCALE) {
                    if (action.axis == null | action.axis == EnumAxis.X) box.scaleX = action.getNumber(box.scaleX);
                    if (action.axis == null | action.axis == EnumAxis.Y) box.scaleY = action.getNumber(box.scaleY);
                    if (action.axis == null | action.axis == EnumAxis.Z) box.scaleZ = action.getNumber(box.scaleZ);
                }
            }
        }
    }

    public void applyToModel(SmoothVector3f box, String anim, String model) {
        for (int i = 0; i < actions.size(); i++) {
            if ((actions.get(i).anim.equalsIgnoreCase(anim) | actions.get(i).anim.equalsIgnoreCase("all")) &
                    actions.get(i).model.equalsIgnoreCase(model)) {
                if (actions.get(i).prop == BendsAction.EnumBoxProperty.ROT) {
                    box.setSmooth(actions.get(i).axis, actions.get(i).getNumber((actions.get(i).axis == EnumAxis.X ? box.vFinal.x : actions.get(i).axis == EnumAxis.Y ? box.vFinal.y : box.vFinal.z)), actions.get(i).smooth);
                }
            }
        }
    }
}
