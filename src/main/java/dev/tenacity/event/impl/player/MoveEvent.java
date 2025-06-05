package dev.tenacity.event.impl.player;

import dev.tenacity.event.Event;
import dev.tenacity.utils.player.MovementUtils;

public class MoveEvent extends Event {

    private double x, y, z;

    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    
    public double getX() {
        return x;
    }

    
    public void setX(double x) {
        this.x = x;
    }

    
    public double getY() {
        return y;
    }

    
    public void setY(double y) {
        this.y = y;
    }

    
    public double getZ() {
        return z;
    }

    
    public void setZ(double z) {
        this.z = z;
    }

    
    public void setSpeed(double speed) {
        MovementUtils.setSpeed(this, speed);
    }

}
