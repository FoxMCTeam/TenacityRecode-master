package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JumpFixEvent extends CancellableEvent {
    private float yaw;
}
