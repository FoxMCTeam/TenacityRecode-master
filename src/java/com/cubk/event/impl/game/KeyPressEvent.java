package com.cubk.event.impl.game;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class KeyPressEvent extends CancellableEvent {

    private final int key;

    public KeyPressEvent(int key) {
        this.key = key;
    }

}
