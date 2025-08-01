package dev.tenacity.event.impl.render;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

public class RenderModelEvent extends CancellableEvent.StateEvent {

    @Getter
    private final EntityLivingBase entity;
    private final Runnable modelRenderer;
    private final Runnable layerRenderer;

    public RenderModelEvent(EntityLivingBase entity, Runnable modelRenderer, Runnable layerRenderer) {
        this.entity = entity;
        this.modelRenderer = modelRenderer;
        this.layerRenderer = layerRenderer;
    }


    public void drawModel() {
        this.modelRenderer.run();
    }


    public void drawLayers() {
        this.layerRenderer.run();
    }

}
