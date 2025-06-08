package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class ClickEvent extends CancellableEvent {
    final boolean fake;

    public ClickEvent(boolean fake) { this.fake = fake; }

}
