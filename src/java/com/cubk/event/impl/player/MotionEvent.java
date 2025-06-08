package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MotionEvent extends CancellableEvent.StateEvent {

    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;

    public MotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }


    public void setRotations(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

}
