package dev.tenacity.event.impl.render;

import dev.tenacity.event.impl.CancellableEvent;

public class ZoomFovEvent extends CancellableEvent {

    private float fov;

    public ZoomFovEvent(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }
}
