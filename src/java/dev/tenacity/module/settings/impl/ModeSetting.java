package dev.tenacity.module.settings.impl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.tenacity.module.settings.Setting;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    @Getter
    public final List<String> modes;
    private int modeIndex;

    @Setter
    @Expose
    @SerializedName("value")
    private String currentMode;

    public ModeSetting(String name, String defaultMode, String... modes) {
        this.name = name;
        this.modes = Arrays.asList(modes);
        this.modeIndex = this.modes.indexOf(defaultMode);
        if (currentMode == null) currentMode = defaultMode;
    }


    public String get() {
        return currentMode;
    }

    public String getMode() {
        return currentMode;
    }

    public boolean is(String mode) {
        return currentMode.equalsIgnoreCase(mode);
    }

    public void set(String currentMode) {
        this.currentMode = currentMode;
    }

    public void cycleForwards() {
        modeIndex++;
        if (modeIndex > modes.size() - 1) modeIndex = 0;
        currentMode = modes.get(modeIndex);
    }

    public void cycleBackwards() {
        modeIndex--;
        if (modeIndex < 0) modeIndex = modes.size() - 1;
        currentMode = modes.get(modeIndex);
    }


    @Override
    public String getConfigValue() {
        return currentMode;
    }

}
