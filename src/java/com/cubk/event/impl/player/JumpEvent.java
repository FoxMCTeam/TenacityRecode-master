package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JumpEvent extends CancellableEvent {
    private float yaw;
    private float jumpMotion;
}
