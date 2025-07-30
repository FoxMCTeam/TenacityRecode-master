package dev.tenacity.event.impl.game;


import dev.tenacity.event.impl.CancellableEvent;
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
