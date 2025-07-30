package dev.tenacity.module.impl.combat;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

public class Velocity extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Watchdog", "Watchdog");
    boolean disableInLobby = true;
    boolean cancelBurning = true;
    boolean cancelExplosion = true;
    boolean cancelWhileFalling = true;
    boolean cancelOffGround = true;
    public Velocity() {
        super("module.combat.velocity", Category.COMBAT, "Reduces your knockback");
        this.addSettings(mode);
    }
    @EventTarget
    public void onPacketEvent(PacketEvent e) {
        this.setSuffix(mode.get());
        if (mode.get().equals("Watchdog")) {
            handleAirMotion(e);
        }
    }

    private void handleAirMotion(PacketEvent e) {
        if (!Utils.nullCheck()  || e.isCancelled()) return;

        if (shouldCancelAllVelocity()) {
            e.cancel();
            return;
        }

        if (e.getPacket() instanceof S12PacketEntityVelocity) {
            handleEntityVelocity((S12PacketEntityVelocity) e.getPacket(), e);
        } else if (e.getPacket() instanceof S27PacketExplosion) {
            handleExplosionVelocity((S27PacketExplosion) e.getPacket(), e);
        }
    }

    private void handleExplosionVelocity(S27PacketExplosion packet, PacketEvent e) {
        if (cancelExplosion || checkAirMotionConditions()) {
            e.cancel();
            return;
        }

        mc.thePlayer.motionX += packet.motionX * 0;
        mc.thePlayer.motionY += packet.motionY * 1;
        mc.thePlayer.motionZ += packet.motionZ * 0;

        e.cancel();
    }

    private boolean checkAirMotionConditions() {
        if (
                (disableInLobby && Utils.isLobby()) ||
                        (cancelBurning && mc.thePlayer.isBurning()) ||
                        (cancelOffGround && !mc.thePlayer.onGround)
        )
            return true;


        return cancelWhileFalling && mc.thePlayer.fallDistance > 0;
    }

    private void handleEntityVelocity(S12PacketEntityVelocity packet, PacketEvent e) {
        if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;

        if (checkAirMotionConditions()) {
            e.cancel();
            return;
        }

        mc.thePlayer.motionX = (packet.getMotionX() / 8000.0) * 0;
        mc.thePlayer.motionY = (packet.getMotionY() / 8000.0) * 1;
        mc.thePlayer.motionZ = (packet.getMotionZ() / 8000.0) * 0;

        e.cancel();
    }

    private boolean shouldCancelAllVelocity() {
        return false;
    }
}
