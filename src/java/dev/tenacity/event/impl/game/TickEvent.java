package dev.tenacity.event.impl.game;

import dev.tenacity.event.Event;

public class TickEvent extends Event.StateEvent {

    private final int ticks;

    public TickEvent(int ticks) {
        this.ticks = ticks;
    }

    
    public int getTicks() {
        return ticks;
    }

}
