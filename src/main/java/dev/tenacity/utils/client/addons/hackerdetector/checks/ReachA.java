package dev.tenacity.utils.client.addons.hackerdetector.checks;

import dev.tenacity.utils.client.addons.hackerdetector.Category;
import dev.tenacity.utils.client.addons.hackerdetector.Detection;
import net.minecraft.entity.player.EntityPlayer;

public class ReachA extends Detection {

    public ReachA() {
        super("Reach A", Category.COMBAT);
    }

    @Override
    public boolean runCheck(EntityPlayer player) {
        return false;
    }
}
