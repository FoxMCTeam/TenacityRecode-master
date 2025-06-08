package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;

@AllArgsConstructor
public class CustomBlockRenderEvent extends CancellableEvent {

    private final BiConsumer<Float, Float> transformFirstPersonItem;
    private final Runnable doBlockTransformations;
    @Getter
    private final float swingProgress, equipProgress;


    public void transformFirstPersonItem(float equipProgress, float swingProgress) {
        this.transformFirstPersonItem.accept(equipProgress, swingProgress);
    }

    
    public void doBlockTransformations() {
        this.doBlockTransformations.run();
    }

}
