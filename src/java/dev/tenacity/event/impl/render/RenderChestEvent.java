package dev.tenacity.event.impl.render;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import net.minecraft.tileentity.TileEntityChest;

public class RenderChestEvent extends CancellableEvent {

    @Getter
    private final TileEntityChest entity;
    private final Runnable chestRenderer;

    public RenderChestEvent(TileEntityChest entity, Runnable chestRenderer) {
        this.entity = entity;
        this.chestRenderer = chestRenderer;
    }

    public void drawChest() {
        this.chestRenderer.run();
    }

}
