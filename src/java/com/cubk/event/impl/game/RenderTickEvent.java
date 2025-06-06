package com.cubk.event.impl.game;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cedo
 * @since 03/30/2022
 */
@AllArgsConstructor
@Getter
public class RenderTickEvent extends CancellableEvent.StateEvent {
    private final float ticks;
}
