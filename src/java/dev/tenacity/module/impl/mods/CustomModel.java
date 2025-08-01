package dev.tenacity.module.impl.mods;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.game.WorldEvent;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.impl.display.HUDMod;
import dev.tenacity.module.settings.impl.ColorSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @see net.minecraft.client.model.ModelPlayer#render(Entity, float, float, float, float, float, float)
 */
public class CustomModel extends Module {

    public static final ResourceLocation amongusModel = new ResourceLocation("Tenacity/Models/amogus.png");
    public static final ResourceLocation rabbitModel = new ResourceLocation("Tenacity/Models/rabbit.png");
    public static final ModeSetting model = new ModeSetting("Model", "Among Us", "Among Us", "Rabbit");
    private static final ModeSetting mogusColorMode = new ModeSetting("Among Us Mode", "Random", "Random", "Sync", "Custom");
    private static final ColorSetting amongusColor = new ColorSetting("Among Us Color", Color.RED);
    private static final Map<Object, Color> entityColorMap = new HashMap<>();
    public static boolean enabled = false;

    public CustomModel() {
        super("module.render.CustomModel", Category.MODS, "Renders an custom model on every player");
        mogusColorMode.addParent(model, modeSetting -> modeSetting.is("Among Us"));
        amongusColor.addParent(mogusColorMode, modeSetting -> modeSetting.is("Custom"));
        addSettings(model, mogusColorMode, amongusColor);
    }

    public static Color getColor(Entity entity) {
        Color color = Color.WHITE;
        switch (mogusColorMode.get()) {
            case "Sync":
                Pair<Color, Color> clientColors = HUDMod.getClientColors();
                color = ColorUtil.interpolateColorsBackAndForth(15, 1, clientColors.getFirst(), clientColors.getSecond(), false);
                break;
            case "Random":
                if (entityColorMap.containsKey(entity)) {
                    color = entityColorMap.get(entity);
                } else {
                    color = ColorUtil.getRandomColor();
                    entityColorMap.put(entity, color);
                }
                break;
            case "Custom":
                color = amongusColor.get();
                break;
        }

        return color;
    }

    public static double getYOffset() {
        switch (model.get()) {
            case "Among Us":
                return 0.25;
        }
        return 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        enabled = false;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        entityColorMap.clear();
        enabled = true;
    }

    @EventTarget
    public void onWorldEvent(WorldEvent event) {
        entityColorMap.clear();
    }


}
