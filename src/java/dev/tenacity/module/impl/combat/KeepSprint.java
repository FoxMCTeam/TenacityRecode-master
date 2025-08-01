package dev.tenacity.module.impl.combat;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.KeepSprintEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;

public final class KeepSprint extends Module {

    public KeepSprint() {
        super("module.combat.keepSprint", Category.COMBAT, "Stops sprint reset after hitting");
    }

    @EventTarget
    public void onKeepSprintEvent(KeepSprintEvent event) {
        event.cancel();
    }

}
