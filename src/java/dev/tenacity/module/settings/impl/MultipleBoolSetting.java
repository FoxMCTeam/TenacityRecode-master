package dev.tenacity.module.settings.impl;

import dev.tenacity.module.settings.Setting;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultipleBoolSetting extends Setting {

    private final Map<String, BooleanSetting> boolSettings;

    public MultipleBoolSetting(String name, String... booleanSettingNames) {
        this.name = name;
        boolSettings = new HashMap<>();
        Arrays.stream(booleanSettingNames).forEach(boolName -> boolSettings.put(boolName.toLowerCase(), new BooleanSetting(boolName, false)));
    }


    public MultipleBoolSetting(String name, BooleanSetting... booleanSettings) {
        this.name = name;
        boolSettings = new HashMap<>();
        Arrays.stream(booleanSettings).forEach(booleanSetting -> boolSettings.put(booleanSetting.name.toLowerCase(), booleanSetting));
    }


    public BooleanSetting getSetting(String settingName) {
        return boolSettings.computeIfAbsent(settingName.toLowerCase(), k -> null);
    }

    public boolean get(String settingName) {
        return boolSettings.get(settingName.toLowerCase()).get();
    }

    public boolean isEnabled(String settingName) {
        return boolSettings.get(settingName.toLowerCase()).get();
    }

    public Collection<BooleanSetting> getBoolSettings() {
        return boolSettings.values();
    }

    @Override
    public HashMap<String, Boolean> getConfigValue() {
        HashMap<String, Boolean> booleans = new HashMap<>();
        for (BooleanSetting booleanSetting : boolSettings.values()) {
            booleans.put(booleanSetting.name, booleanSetting.get());
        }
        return booleans;
    }

}
