package dev.tenacity.module.impl.player;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.player.SafeWalkEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;

public final class SafeWalk extends Module {
    @EventTarget
    public void onSafeWalkEvent(SafeWalkEvent e) {
        if(mc.thePlayer == null) return;
        e.setSafe(true);
    }
    public SafeWalk() {
        super("SafeWalk", Category.PLAYER, "prevents walking off blocks");
    }

}
