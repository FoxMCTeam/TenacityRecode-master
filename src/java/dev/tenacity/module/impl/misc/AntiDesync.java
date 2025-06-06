package dev.tenacity.module.impl.misc;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.network.PacketSendEvent;
import com.cubk.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@SuppressWarnings("unused")
public final class AntiDesync extends Module {

    private int slot;

    public AntiDesync() {
        super("AntiDesync", Category.MISC, "pervents desync client side");
    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent event) {
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
