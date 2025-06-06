package com.cubk.event.impl.player;


import com.cubk.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SafeWalkEvent extends CancellableEvent {

    private boolean safe;


}
