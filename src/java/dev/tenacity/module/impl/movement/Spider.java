package dev.tenacity.module.impl.movement;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;

public final class Spider extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", "Vanilla", "Verus");

    public Spider() {
        super("module.movement.Spider", Category.MOVEMENT, "Climbs you up walls like a spider");
        addSettings(mode);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        setSuffix(mode.get());
        if (mc.thePlayer.isCollidedHorizontally) {
            if (!mc.thePlayer.onGround && mc.thePlayer.isCollidedVertically) return;
            switch (mode.get()) {
                case "Vanilla":
                    mc.thePlayer.jump();
                    break;
                case "Verus":
                    if (mc.thePlayer.ticksExisted % 3 == 0)
                        mc.thePlayer.motionY = 0.42f;
                    break;
            }
        }
    }
}
