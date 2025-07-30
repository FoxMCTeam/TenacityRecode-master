package dev.tenacity.event.impl.player;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JumpEvent extends CancellableEvent {
    private float yaw;
    private float jumpMotion;
}
