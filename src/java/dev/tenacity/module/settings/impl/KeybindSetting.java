package dev.tenacity.module.settings.impl;

import dev.tenacity.module.settings.Setting;
import lombok.Setter;
import org.lwjglx.input.Keyboard;

@Setter
public class KeybindSetting extends Setting {

    private int code;

    public KeybindSetting(int code) {
        this.name = "Keybind";
        this.code = code;
    }

    public int get() {
        return code == -1 ? Keyboard.KEY_NONE : code;
    }

    public int getCode() {
        return code == -1 ? Keyboard.KEY_NONE : code;
    }

    @Override
    public Integer getConfigValue() {
        return this.get();
    }

}
