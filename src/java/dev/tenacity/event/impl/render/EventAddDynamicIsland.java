package dev.tenacity.event.impl.render;

import dev.tenacity.event.EventState;
import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventAddDynamicIsland extends CancellableEvent {
    public EventState state;

    public EventAddDynamicIsland(EventState state) {
        this.state = state;
    }

    public CancellableEvent setState(EventState state) {
        return new EventAddDynamicIsland(state);
    }
}
