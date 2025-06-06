package dev.tenacity.utils.client.addons.hackerdetector.checks;

import dev.tenacity.utils.client.addons.hackerdetector.Category;
import dev.tenacity.utils.client.addons.hackerdetector.Detection;
import dev.tenacity.utils.client.addons.hackerdetector.utils.MovementUtils;
import net.minecraft.entity.player.EntityPlayer;

public class FlightA extends Detection {

    public FlightA() {
        super("Flight A", Category.MOVEMENT);
    }

    @Override
    public boolean runCheck(EntityPlayer player) {
        return !player.onGround && player.motionY == 0 && MovementUtils.isMoving(player);
    }
}
