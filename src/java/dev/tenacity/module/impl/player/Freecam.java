package dev.tenacity.module.impl.player;

import dev.tenacity.event.annotations.EventTarget;

import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.event.impl.player.BoundingBoxEvent;
import dev.tenacity.event.impl.player.PushOutOfBlockEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class Freecam extends Module {

    public Freecam() {
        super("module.player.Freecam", Category.PLAYER, "allows you to look around freely");
    }

    @EventTarget
    public void onBoundingBoxEvent(BoundingBoxEvent event) {
        if (mc.thePlayer != null) {
            event.cancel();
        }
    }

    @EventTarget
    public void onPushOutOfBlockEvent(PushOutOfBlockEvent event) {
        if (mc.thePlayer != null) {
            event.cancel();
        }
    }

    @EventTarget
    public void onPacketEvent(PacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer) {
            event.cancel();
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.capabilities.allowFlying = true;
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.capabilities.allowFlying = false;
            mc.thePlayer.capabilities.isFlying = false;
        }
        super.onDisable();
    }

}
