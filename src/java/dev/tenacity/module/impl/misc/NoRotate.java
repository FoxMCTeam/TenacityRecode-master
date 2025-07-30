package dev.tenacity.module.impl.misc;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public final class NoRotate extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Normal", "Normal", "Cancel");
    private final BooleanSetting fakeUpdate = new BooleanSetting("Fake Update", false);

    public NoRotate() {
        super("module.misc.noRotate", Category.MISC, "Prevents servers from rotating you");
        this.addSettings(fakeUpdate);
    }

    @EventTarget
    public void onPacketEvent(PacketEvent e) {
        if (mc.thePlayer == null) return;
        if (e.getPacket() instanceof S08PacketPlayerPosLook packet) {
            switch (mode.get()) {
                case "Normal":
                    packet.setYaw(mc.thePlayer.rotationYaw);
                    packet.setPitch(mc.thePlayer.rotationPitch);
                    break;
                case "Cancel":
                    e.cancel();
                    break;
            }

            if (fakeUpdate.get()) {
                PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(
                        mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ,
                        packet.getYaw(), packet.getPitch(), mc.thePlayer.onGround));
            }
        }
    }

}
