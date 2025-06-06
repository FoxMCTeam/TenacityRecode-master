package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;

public class ClickEvent extends CancellableEvent {
    boolean fake;

    public ClickEvent(boolean fake) { this.fake = fake; }

    public boolean isFake() { return fake; }
}
