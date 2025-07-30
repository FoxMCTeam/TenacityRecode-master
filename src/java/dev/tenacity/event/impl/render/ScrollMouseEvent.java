package dev.tenacity.event.impl.render;

import dev.tenacity.event.impl.CancellableEvent;

public class ScrollMouseEvent extends CancellableEvent {

    private int amount;

    public ScrollMouseEvent(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
