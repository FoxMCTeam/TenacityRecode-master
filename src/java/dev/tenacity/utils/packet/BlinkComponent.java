package dev.tenacity.utils.packet;

import dev.tenacity.event.annotations.EventTarget;

import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.utils.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class BlinkComponent implements Utils {
    private static final PacketCollector packetCollector = new PacketCollector(0, 1000);
    private static boolean state;

    public BlinkComponent() {
    }

    @EventTarget
    public void onPacketEvent(PacketEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isDead || mc.isSingleplayer() || mc.thePlayer.ticksExisted < 50) {
            packetCollector.removeAll();
            state = false;
            return;
        }

        if (event.getPacket() instanceof C03PacketPlayer && state) {
            packetCollector.add(event.getPacket());
            event.cancel();
        }
    }

    public static void startBlink() {
        state = true;
    }

    public static void stopBlink(CollectorType type) {
        packetCollector.releasePackets(type);
    }
}
