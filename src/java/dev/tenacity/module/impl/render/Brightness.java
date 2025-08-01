package dev.tenacity.module.impl.render;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;

public final class Brightness extends Module {

    public Brightness() {
        super("module.render.Brightness", Category.RENDER, "changes the game brightness");
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        mc.gameSettings.gammaSetting = 100;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = 0;
        super.onDisable();
    }

}
