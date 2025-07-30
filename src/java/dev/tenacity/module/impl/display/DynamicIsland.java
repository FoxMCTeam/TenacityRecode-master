package dev.tenacity.module.impl.display;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.render.Render2DEvent;
import dev.tenacity.event.impl.render.ShaderEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.MultipleBoolSetting;
import dev.tenacity.utils.render.island.IslandRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class DynamicIsland extends Module {
    public static ModeSetting colorMode = new ModeSetting("Color Mode", "Black", "Colorful", "Black");
    public static MultipleBoolSetting textMode = new MultipleBoolSetting(
            "Text",
            new BooleanSetting("Client Name", true),
            new BooleanSetting("User Name", true),
            new BooleanSetting("Logo", true)
    );
    public static BooleanSetting notification = new BooleanSetting("Notification", true);

    public static BooleanSetting targetHUD = new BooleanSetting("TargetHUD", true);
    public static BooleanSetting chestStealer = new BooleanSetting("Stealing", true);
    public static BooleanSetting gapple = new BooleanSetting("Gapple Counts", true);
    public static BooleanSetting speed = new BooleanSetting("Speed", true);
    public static BooleanSetting scaffold = new BooleanSetting("Scaffold Block Counts", true);
    public static BooleanSetting disabler = new BooleanSetting("Disabler Process", true);

    public DynamicIsland() {
        super("DynamicIsland", Category.DISPLAY, "Displays a floating and adaptive UI element similar to Apple's Dynamic Island");
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent event) {
        IslandRenderer.INSTANCE.render(new ScaledResolution(mc), false);
    }

    @EventTarget
    public void onShaderEvent(ShaderEvent event) {
        IslandRenderer.INSTANCE.render(new ScaledResolution(mc), true);
    }
}
