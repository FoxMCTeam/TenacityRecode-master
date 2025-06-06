package dev.tenacity.utils.client.addons.api.bindings;

import dev.tenacity.utils.Utils;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;
import java.util.stream.Collectors;


public class WorldBinding implements Utils {

    public void setTimer(float speed) {
        mc.timer.timerSpeed = speed;
    }

    public boolean isSinglePlayer() {
        return mc.isSingleplayer();
    }

    public float timer() {
        return mc.timer.timerSpeed;
    }

    public List<EntityLivingBase> getLivingEntities() {
        return mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityLivingBase).map(entity -> (EntityLivingBase) entity).collect(Collectors.toList());
    }

}
