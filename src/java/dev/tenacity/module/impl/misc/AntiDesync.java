package dev.tenacity.module.impl.misc;

import dev.tenacity.event.annotations.EventTarget;

import dev.tenacity.event.impl.network.PacketEvent;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@SuppressWarnings("unused")
public final class AntiDesync extends Module {

    private int slot;

    public AntiDesync() {
        super("module.misc.antiDesync", Category.MISC, "pervents desync client side");
    }

    @EventTarget
    public void onPacketEvent(PacketEvent event) {
        if (event.getPacket() instanceof C09PacketHeldItemChange) {
            slot = ((C09PacketHeldItemChange) event.getPacket()).getSlotId();
        }
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (slot != mc.thePlayer.inventory.currentItem && slot != -1) {
            PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

}
