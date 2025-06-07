package dev.tenacity.module.impl.combat;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.player.AttackEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public final class SuperKnockback extends Module {

    public SuperKnockback() {
        super("module.combat.superKB", Category.COMBAT, "Makes the player your attacking take extra knockback");
    }

    @EventTarget
    public void onAttackEvent(AttackEvent event) {
        if (event.getTargetEntity() != null) {
            if (mc.thePlayer.isSprinting())
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));

            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        }
    }
}
