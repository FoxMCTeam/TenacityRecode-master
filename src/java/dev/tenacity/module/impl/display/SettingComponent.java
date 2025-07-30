package dev.tenacity.module.impl.display;

import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.StringSetting;

public class SettingComponent extends Module {
    public static final ModeSetting language = new ModeSetting("Language Mode", "en_US", "en_US", "ru_RU", "zh_CN", "zh_HK", "de_DE", "fr_fR");
    public static final StringSetting clientName = new StringSetting("Client Name");
    public static final BooleanSetting customFont = new BooleanSetting("Custom Font", true);
    public static final ModeSetting movementFixMode = new ModeSetting("Movement fix Mode", "Traditional", "Off", "Normal", "Traditional", "Backwards Sprint");
    public static final ModeSetting kAMovementFixMode = new ModeSetting("Kill Aura Movement fix Mode", "Traditional", "Off", "Normal", "Traditional", "Backwards Sprint");
    public static final BooleanSetting reload = new BooleanSetting("Reload Client", true);
    public SettingComponent() {
        super("module.display.settingComponent", Category.DISPLAY, "fuck u mom");
        addSettings(clientName, language, customFont, movementFixMode, kAMovementFixMode, reload);
    }
}
