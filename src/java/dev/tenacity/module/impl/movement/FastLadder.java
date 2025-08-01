package dev.tenacity.module.impl.movement;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.player.MotionEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import net.minecraft.network.play.client.C03PacketPlayer;

public class FastLadder extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Motion", "Motion", "Timer", "Position");
    private final NumberSetting speed = new NumberSetting("Speed", 1.5, 5, 0.1, 0.01);

    public FastLadder() {
        super("module.movement.FastLadder", Category.MOVEMENT, "Climbs up ladders faster than normal");
        this.addSettings(mode, speed);
    }

    @EventTarget
    public void onMotionEvent(MotionEvent event) {
        this.setSuffix(mode.get());
        if (mc.thePlayer.isOnLadder()) {
            switch (mode.get()) {
                case "Timer":
                    mc.timer.timerSpeed = speed.get().floatValue();
                    break;
                case "Motion":
                    mc.thePlayer.motionY = speed.get();
                    break;
                case "Position":
                    mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                            mc.thePlayer.posX, mc.thePlayer.posY + speed.get(), mc.thePlayer.posZ,
                            mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + speed.get(), mc.thePlayer.posZ);
                    break;
            }
        } else {
            mc.timer.timerSpeed = 1;
        }
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

}
