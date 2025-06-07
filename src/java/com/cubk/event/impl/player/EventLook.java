package com.cubk.event.impl.player;

import com.cubk.event.impl.CancellableEvent;
import dev.tenacity.utils.client.addons.vector.Vector2f;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventLook extends CancellableEvent {
    private Vector2f rotation;

    public EventLook(Vector2f rotation) {
        this.rotation = rotation;
    }

}

