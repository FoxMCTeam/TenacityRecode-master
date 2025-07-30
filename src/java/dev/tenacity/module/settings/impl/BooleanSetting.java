package dev.tenacity.module.settings.impl;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import dev.tenacity.module.settings.Setting;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BooleanSetting extends Setting {

    @Expose
    @SerializedName("name")
    private boolean state;

    public BooleanSetting(String name, boolean state) {
        this.name = name;
        this.state = state;
    }

    public boolean get() {
        return state;
    }

    public boolean isEnabled() {
        return state;
    }

    public void toggle() {
        setState(!get());
    }

    public void set(boolean b) {
        state = b;
    }

    @Override
    public Boolean getConfigValue() {
        return get();
    }
}
