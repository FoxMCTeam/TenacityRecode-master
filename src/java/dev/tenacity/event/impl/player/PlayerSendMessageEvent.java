package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;

@Getter
public class PlayerSendMessageEvent extends CancellableEvent {
    private final String message;

    public PlayerSendMessageEvent(String message) {
        this.message = message;
    }

}
