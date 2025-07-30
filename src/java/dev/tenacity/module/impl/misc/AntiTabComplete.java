package dev.tenacity.module.impl.misc;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.network.PacketEvent;

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
    public void onPacketEvent(PacketEvent event) {
        if (event.getPacket() instanceof S3APacketTabComplete) {
            event.cancel();
        }

        if (event.getPacket() instanceof C14PacketTabComplete) {
            event.cancel();
        }
    }

}
