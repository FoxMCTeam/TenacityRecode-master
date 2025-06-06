package com.cubk.event.impl.render;


import com.cubk.event.impl.CancellableEvent;
import net.minecraft.client.gui.ScaledResolution;

public class Render2DEvent extends CancellableEvent {

    public ScaledResolution sr;
    private float width, height;

    public Render2DEvent(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Render2DEvent(ScaledResolution sr) {
        this.sr = sr;
    }

    
    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    
    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

}
