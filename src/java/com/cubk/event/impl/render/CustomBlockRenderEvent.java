package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;

import java.util.function.BiConsumer;

@AllArgsConstructor
public class CustomBlockRenderEvent extends CancellableEvent {

    private final BiConsumer<Float, Float> transformFirstPersonItem;
    private final Runnable doBlockTransformations;
    private final float swingProgress, equipProgress;

    
    public float getSwingProgress() {
        return swingProgress;
    }

    
    public float getEquipProgress() {
        return equipProgress;
    }

    
    public void transformFirstPersonItem(float equipProgress, float swingProgress) {
        this.transformFirstPersonItem.accept(equipProgress, swingProgress);
    }

    
    public void doBlockTransformations() {
        this.doBlockTransformations.run();
    }

}
