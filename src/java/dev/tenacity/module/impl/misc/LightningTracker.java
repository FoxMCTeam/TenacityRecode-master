package dev.tenacity.module.impl.misc;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.network.PacketReceiveEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.utils.player.ChatUtil;
import net.minecraft.network.play.server.S29PacketSoundEffect;

public final class LightningTracker extends Module {

    public LightningTracker() {
        super("module.misc.lightningTracker", Category.MISC, "detects lightning");
    }

    @EventTarget
    public void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S29PacketSoundEffect soundPacket) {
            if (soundPacket.getSoundName().equals("ambient.weather.thunder")) {
                ChatUtil.print(String.format("Lightning detected at (%s, %s, %s)", (int) soundPacket.getX(), (int) soundPacket.getY(), (int) soundPacket.getZ()));
            }
        }
    }

}
