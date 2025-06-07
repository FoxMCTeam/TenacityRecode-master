package dev.tenacity.module.impl.player;

import com.cubk.event.annotations.EventTarget;
import com.cubk.event.impl.network.PacketSendEvent;
import com.cubk.event.impl.player.MotionEvent;
import com.cubk.event.impl.render.Render3DEvent;
import dev.tenacity.Client;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.render.Breadcrumbs;
import dev.tenacity.module.settings.ParentAttribute;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.utils.server.PacketUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Blink extends Module {
    final ConcurrentLinkedQueue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    private final BooleanSetting pulse = new BooleanSetting("Pulse", false);
    private final NumberSetting delayPulse = new NumberSetting("Tick Delay", 20, 100, 4, 1);
    List<Vec3> path = new ArrayList<>();
    private EntityOtherPlayerMP blinkEntity;

    public Blink() {
        super("module.player.Blink", Category.PLAYER, "holds movement packets");
        delayPulse.addParent(pulse, ParentAttribute.BOOLEAN_CONDITION);
        this.addSettings(pulse, delayPulse);
    }

    @EventTarget
    public void onPacketSendEvent(PacketSendEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isDead || mc.isSingleplayer() || mc.thePlayer.ticksExisted < 50) {
            packets.clear();
            return;
        }

        if (event.getPacket() instanceof C03PacketPlayer) {
            packets.add(event.getPacket());
            event.cancel();
        }

        if (pulse.isEnabled()) {
            if (!packets.isEmpty() && mc.thePlayer.ticksExisted % delayPulse.getValue().intValue() == 0 && Math.random() > 0.1) {
                packets.forEach(PacketUtils::sendPacketNoEvent);
                packets.clear();
            }
        }
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        if (event.isPre()) {
            if (mc.thePlayer.ticksExisted < 50) return;

            if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
                path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            }

            if (pulse.isEnabled()) {
                while (path.size() > delayPulse.getValue().intValue()) {
                    path.remove(0);
                }
            }

            if (pulse.isEnabled() && blinkEntity != null) {
                mc.theWorld.removeEntityFromWorld(blinkEntity.getEntityId());
            }
        }
    }

    @EventTarget
    public void onRender3DEvent(Render3DEvent event) {
        Client.INSTANCE.getModuleManager().getModule(Breadcrumbs.class).renderLine(path);
    }

    @Override
    public void onEnable() {
        path.clear();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        packets.forEach(PacketUtils::sendPacketNoEvent);
        packets.clear();
        super.onDisable();
    }
}
