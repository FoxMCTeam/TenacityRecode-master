package dev.tenacity.event.impl.game;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class TickEvent extends CancellableEvent.StateEvent {

    private final int ticks;

    public TickEvent(int ticks) {
        this.ticks = ticks;
    }


}
