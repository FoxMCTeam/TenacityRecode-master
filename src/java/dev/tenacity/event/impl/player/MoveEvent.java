package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import dev.tenacity.utils.player.MovementUtils;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MoveEvent extends CancellableEvent {

    private double x, y, z;

    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setSpeed(double speed) {
        MovementUtils.setSpeed(this, speed);
    }

}
