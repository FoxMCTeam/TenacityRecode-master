package dev.tenacity.event.impl.player;

import dev.tenacity.event.Event;

public class PlayerSendMessageEvent extends Event {
    private final String message;

    public PlayerSendMessageEvent(String message) {
        this.message = message;
    }

    
    public String getMessage() {
        return message;
    }

}
