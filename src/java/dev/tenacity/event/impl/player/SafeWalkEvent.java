package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SafeWalkEvent extends CancellableEvent {

    private boolean safe;


}
