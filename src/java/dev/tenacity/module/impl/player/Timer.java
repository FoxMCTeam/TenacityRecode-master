package dev.tenacity.module.impl.player;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.NumberSetting;

@SuppressWarnings("unused")
public final class Timer extends Module {

    private final NumberSetting amount = new NumberSetting("Amount", 1, 10, 0.1, 0.1);

    public Timer() {
        super("module.player.Timer", Category.PLAYER, "changes game speed");
        this.addSettings(amount);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        mc.timer.timerSpeed = amount.get().floatValue();
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

}
