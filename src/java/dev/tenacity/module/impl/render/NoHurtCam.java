package dev.tenacity.module.impl.render;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.render.HurtCamEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;

public class NoHurtCam extends Module {

    public NoHurtCam() {
        super("module.render.NoHurtCam", Category.RENDER, "removes shaking after being hit");
    }

    @EventTarget
    public void onHurtCamEvent(HurtCamEvent e) {
        e.cancel();
    }

}
