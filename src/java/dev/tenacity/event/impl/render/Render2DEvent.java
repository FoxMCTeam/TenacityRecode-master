package dev.tenacity.event.impl.render;


import dev.tenacity.event.impl.CancellableEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;

public class Render2DEvent extends CancellableEvent {

    public ScaledResolution sr;
    @Setter
    @Getter
    private float width, height;

    public Render2DEvent(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public Render2DEvent(ScaledResolution sr) {
        this.sr = sr;
    }


}
