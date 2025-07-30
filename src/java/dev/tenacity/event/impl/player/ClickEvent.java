package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class ClickEvent extends CancellableEvent {
    final boolean fake;

    public ClickEvent(boolean fake) {
        this.fake = fake;
    }

}
