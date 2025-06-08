package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Render3DEvent extends CancellableEvent {

    private float ticks;

    public Render3DEvent(float ticks) {
        this.ticks = ticks;
    }


}
