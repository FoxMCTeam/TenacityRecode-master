package dev.tenacity.event.impl.game;


import dev.tenacity.event.impl.CancellableEvent;
import net.minecraft.world.World;

/**
 * WorldEvent is fired when an event involving the world occurs.
 * If a method utilizes this {@link CancellableEvent} as its parameter, the method will
 * receive every child event of this class.
 * {@link #world} contains the World this event is occuring in.
 **/
public class WorldEvent extends CancellableEvent {
    public final World world;

    public WorldEvent(World world) {
        this.world = world;
    }
}
