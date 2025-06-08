package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import dev.tenacity.utils.player.MovementUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

@Getter
@Setter
@AllArgsConstructor
public class PlayerMoveUpdateEvent extends CancellableEvent {

    private float strafe, forward, friction, yaw, pitch;

    public void applyMotion(double speed, float strafeMotion) {
        float remainder = 1 - strafeMotion;
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        strafeMotion *= 0.91F;
        if (player.onGround) {
            MovementUtils.setSpeed(speed);
        } else {
            player.motionX = player.getMotionX() * strafeMotion;
            player.motionZ = player.getMotionZ() * strafeMotion;
            friction = (float) speed * remainder;
        }
    }

}