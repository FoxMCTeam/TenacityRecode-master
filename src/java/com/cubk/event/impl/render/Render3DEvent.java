package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;

public class Render3DEvent extends CancellableEvent {

    private float ticks;

    public Render3DEvent(float ticks) {
        this.ticks = ticks;
    }

    
    public float getTicks() {
        return ticks;
    }

    public void setTicks(float ticks) {
        this.ticks = ticks;
    }

}
