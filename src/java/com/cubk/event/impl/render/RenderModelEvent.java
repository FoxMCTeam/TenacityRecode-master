package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;
import net.minecraft.entity.EntityLivingBase;

public class RenderModelEvent extends CancellableEvent.StateEvent {

    private final EntityLivingBase entity;
    private final Runnable modelRenderer;
    private final Runnable layerRenderer;

    public RenderModelEvent(EntityLivingBase entity, Runnable modelRenderer, Runnable layerRenderer) {
        this.entity = entity;
        this.modelRenderer = modelRenderer;
        this.layerRenderer = layerRenderer;
    }

    
    public EntityLivingBase getEntity() {
        return entity;
    }

    
    public void drawModel() {
        this.modelRenderer.run();
    }

    
    public void drawLayers() {
        this.layerRenderer.run();
    }

}
