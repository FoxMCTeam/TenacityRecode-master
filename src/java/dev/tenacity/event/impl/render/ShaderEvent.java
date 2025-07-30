package dev.tenacity.event.impl.render;


import dev.tenacity.event.impl.CancellableEvent;
import dev.tenacity.module.settings.impl.MultipleBoolSetting;
import lombok.Getter;

@Getter
public class ShaderEvent extends CancellableEvent {

    private final boolean bloom;

    private final MultipleBoolSetting bloomOptions;

    public ShaderEvent(boolean bloom, MultipleBoolSetting bloomOptions) {
        this.bloom = bloom;
        this.bloomOptions = bloomOptions;
    }


}
