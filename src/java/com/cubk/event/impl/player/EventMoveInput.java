package com.cubk.event.impl.player;

import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventMoveInput extends CancellableEvent {

    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;

    public EventMoveInput(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }
}

