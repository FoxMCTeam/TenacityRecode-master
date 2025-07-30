package dev.tenacity.module.impl.misc;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.TickEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C16PacketClientStatus;

public final class AutoRespawn extends Module {

    public AutoRespawn() {
        super("module.misc.autoRespawn", Category.MISC, "automatically respawn");
    }

    @EventTarget
    public void onTickEvent(TickEvent event) {
        if (mc.thePlayer != null && mc.thePlayer.isDead) {
            PacketUtils.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
        }
    }

}
