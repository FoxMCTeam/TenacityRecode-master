package dev.tenacity.module.impl.misc;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.network.PacketReceiveEvent;
import com.cubk.event.impl.network.PacketSendEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;

@SuppressWarnings("unused")
public final class AntiTabComplete extends Module {

    public AntiTabComplete() {
        super("module.misc.antiTabComplete", Category.MISC, "prevents you from tab completing");
    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent event) {
        if (event.getPacket() instanceof C14PacketTabComplete) {
            event.cancel();
        }
    }

    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S3APacketTabComplete) {
            event.cancel();
        }
    }

}
