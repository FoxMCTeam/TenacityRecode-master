package dev.tenacity.module.impl.display;

import dev.tenacity.event.annotations.EventTarget;
import dev.tenacity.event.impl.render.Render2DEvent;
import dev.tenacity.event.impl.render.ShaderEvent;
import dev.tenacity.Client;
import dev.tenacity.i18n.Localization;
import dev.tenacity.module.Category;
import dev.tenacity.module.Module;
import dev.tenacity.module.ModuleManager;
import dev.tenacity.module.settings.ParentAttribute;
import dev.tenacity.module.settings.impl.BooleanSetting;
import dev.tenacity.module.settings.impl.ModeSetting;
import dev.tenacity.module.settings.impl.MultipleBoolSetting;
import dev.tenacity.module.settings.impl.NumberSetting;
import dev.tenacity.utils.animations.Animation;
import dev.tenacity.utils.animations.Direction;
import dev.tenacity.utils.font.AbstractFontRenderer;
import dev.tenacity.utils.objects.Dragging;
import dev.tenacity.utils.render.ColorUtil;
import dev.tenacity.utils.render.RenderUtil;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StringUtils;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class ArrayListMod extends Module {

    public final MultipleBoolSetting hideModules = new MultipleBoolSetting("Hide Modules",
            new BooleanSetting("Combat", false),
            new BooleanSetting("Movement", false),
            new BooleanSetting("Render", false),
            new BooleanSetting("Display", true),
            new BooleanSetting("Player", false),
            new BooleanSetting("Misc", false),
            new BooleanSetting("Scripts", false));
    public final NumberSetting height = new NumberSetting("Height", 11, 20, 9, .5f);
    private final ModeSetting textShadow = new ModeSetting("Text Shadow", "Black", "Colored", "Black", "None");
    private final ModeSetting rectangle = new ModeSetting("Rectangle", "Top", "None", "Top", "Side", "Outline");
    private final BooleanSetting partialGlow = new BooleanSetting("Partial Glow", true);
    private final BooleanSetting minecraftFont = new BooleanSetting("Minecraft Font", false);
    private final MultipleBoolSetting fontSettings = new MultipleBoolSetting("Font Settings",
            new BooleanSetting("Bold", false),
            new BooleanSetting("Small Font", false), minecraftFont);
    public static final ModeSetting animation = new ModeSetting("Animation", "Scale in", "Move in", "Scale in", "None");
    private final NumberSetting colorIndex = new NumberSetting("Color Separation", 20, 100, 5, 1);
    private final NumberSetting colorSpeed = new NumberSetting("Color Speed", 15, 30, 2, 1);
    private final BooleanSetting background = new BooleanSetting("Background", true);
    private final BooleanSetting backgroundColor = new BooleanSetting("Background Color", false);
    private final NumberSetting backgroundAlpha = new NumberSetting("Background Alpha", .35, 1, 0, .01);

    public AbstractFontRenderer font = duckSansFont.size(20);
    public List<Module> modules;
    public Dragging arraylistDrag = Client.INSTANCE.createDrag(this, "arraylist", 2, 1);
    public String longest = "";
    Module lastModule;
    int lastCount;

    public ArrayListMod() {
        super("module.display.arrayList", Category.DISPLAY, "Displays your active modules");
        addSettings(hideModules, rectangle, partialGlow, textShadow, fontSettings, height, animation,
                colorIndex, colorSpeed, background, backgroundColor, backgroundAlpha);
        backgroundAlpha.addParent(background, ParentAttribute.BOOLEAN_CONDITION);
        backgroundColor.addParent(background, ParentAttribute.BOOLEAN_CONDITION);
        partialGlow.addParent(rectangle, modeSetting -> !modeSetting.is("None"));
        if (!enabled) this.toggleSilent();
    }

    public void getModulesAndSort() {
        if (modules == null || ModuleManager.reloadModules) {
            modules = getModules();
        }
        modules.sort(Comparator.<Module>comparingDouble(m -> {
            String name = HUDMod.get(Localization.get(m.getName()) + (m.hasMode() ? " " + m.getSuffix() : ""));
            return font.getStringWidth(applyText(name));
        }).reversed());
    }

    private static List<Module> getModules() {
        List<Class<? extends Module>> hiddenModules = Client.INSTANCE.getModuleManager().hiddenModules;
        List<Module> commandHiddenModules = Client.INSTANCE.getModuleManager().commandHiddenModules;
        List<Module> moduleList = Client.INSTANCE.getModuleManager().getModules();
        moduleList.removeIf(module -> hiddenModules.stream().anyMatch(moduleClass -> moduleClass == module.getClass()));
        moduleList.removeIf(module -> commandHiddenModules.stream().anyMatch(moduleClass -> moduleClass == module));
        return moduleList;
    }

    @EventTarget
    public void onShaderEvent(ShaderEvent e) {
        if (modules == null) return;
        float yOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        int count = 0;
        for (Module module : modules) {
            final Animation moduleAnimation = module.getAnimation();
            if (!Client.INSTANCE.getModuleManager().canRender(this, module)) continue;
            if ((!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) || (!module.isEnabled() && animation.is("None"))) continue;

            String displayText = HUDMod.get(Localization.get(module.getName()) + (module.hasMode() ? " §7" + module.getSuffix() : ""));
            displayText = applyText(displayText);
            float textWidth = font.getStringWidth(displayText);

            float xValue = sr.getScaledWidth() - (arraylistDrag.getX());

            boolean flip = xValue <= sr.getScaledWidth() / 2f;
            float x = flip ? xValue : sr.getScaledWidth() - (textWidth + arraylistDrag.getX());

            float y = yOffset + arraylistDrag.getY();

            float heightVal = height.get().floatValue() + 1;
            boolean scaleIn = false;
            switch (animation.get()) {
                case "Move in":
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getScaledWidth() - (arraylistDrag.getX() + textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                    break;
                case "Scale in":
                    if (!moduleAnimation.isDone()) {
                        RenderUtil.scaleStart(x + font.getStringWidth(displayText) / 2f, y + heightVal / 2 - font.getHeight() / 2f, moduleAnimation.getOutput().floatValue());
                    }
                    scaleIn = true;
                    break;
            }


            int index = (int) (count * colorIndex.get());
            Pair<Color, Color> colors = HUDMod.getClientColors();

            Color textcolor = ColorUtil.interpolateColorsBackAndForth(colorSpeed.get().intValue(), index, colors.getFirst(), colors.getSecond(), false);

            if (HUDMod.isRainbowTheme()) {
                textcolor = ColorUtil.rainbow(colorSpeed.get().intValue(), index, HUDMod.color1.getRainbow().getSaturation(), 1, 1);
            }

            if (background.get()) {
                float offset = minecraftFont.get() ? 4 : 5;
                int rectColor = e.getBloomOptions().getSetting("Arraylist").get() ? textcolor.getRGB() : (rectangle.get().equals("Outline") && partialGlow.get() ? textcolor.getRGB() : Color.BLACK.getRGB());


                Gui.drawRect2(x - 2, y, font.getStringWidth(displayText) + offset, heightVal,
                        scaleIn ? ColorUtil.applyOpacity(rectColor, moduleAnimation.getOutput().floatValue()) : rectColor);

                float offset2 = minecraftFont.get() ? 1 : 0;

                int rectangleColor = partialGlow.get() ? textcolor.getRGB() : Color.BLACK.getRGB();

                if (scaleIn) {
                    rectangleColor = ColorUtil.applyOpacity(rectangleColor, moduleAnimation.getOutput().floatValue());
                }

                switch (rectangle.get()) {
                    case "Outline":
                    default:
                        break;
                    case "Top":
                        if (count == 0) {
                            Gui.drawRect2(x - 2, y - 1, textWidth + 5 - (offset2), 9, rectangleColor);
                        }
                        break;
                    case "Side":
                        if (flip) {
                            Gui.drawRect2(x - 3, y, 9, heightVal, textcolor.getRGB());
                        } else {
                            Gui.drawRect2(x + textWidth - 7, y, 9, heightVal, rectangleColor);
                        }
                        break;
                }
            }


            if (animation.is("Scale in") && !moduleAnimation.isDone()) {
                RenderUtil.scaleEnd();
            }

            yOffset += animation.is("None") ? 1 * heightVal : moduleAnimation.getOutput().floatValue() * heightVal;
            count++;
        }
    }

    @EventTarget
    public void onRender2DEvent(Render2DEvent e) {
        font = getFont();
        getModulesAndSort();

        String longestModule = "";
        float longestWidth = 0;
        double yOffset = 0;
        ScaledResolution sr = new ScaledResolution(mc);
        int count = 0;
        for (Module module : modules) {
            final Animation moduleAnimation = module.getAnimation();
            moduleAnimation.setDirection(module.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
            if ((!module.isEnabled() && moduleAnimation.finished(Direction.BACKWARDS)) || (!module.isEnabled() && animation.is("None"))) continue;
            if (!Client.INSTANCE.getModuleManager().canRender(this, module)) continue;

            String displayText = HUDMod.get(Localization.get(module.getName()) + (module.hasMode() ? (module.getCategory().equals(Category.SCRIPTS) ? " §c" : " §7") + module.getSuffix() : ""));
            displayText = applyText(displayText);
            float textWidth = font.getStringWidth(displayText);

            if (textWidth > longestWidth) {
                longestModule = displayText;
                longestWidth = textWidth;
            }

            double xValue = sr.getScaledWidth() - (arraylistDrag.getX());


            boolean flip = xValue <= sr.getScaledWidth() / 2f;
            float x = (float) (flip ? xValue : sr.getScaledWidth() - (textWidth + arraylistDrag.getX()));


            float alphaAnimation = 1;

            float y = (float) (yOffset + arraylistDrag.getY());

            float heightVal = (float) (height.get() + 1);

            switch (animation.get()) {
                case "Move in":
                    if (flip) {
                        x -= Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (sr.getScaledWidth() - (arraylistDrag.getX() - textWidth)));
                    } else {
                        x += Math.abs((moduleAnimation.getOutput().floatValue() - 1) * (arraylistDrag.getX() + textWidth));
                    }
                    break;
                case "Scale in":
                    if (!moduleAnimation.isDone()) {
                        RenderUtil.scaleStart(x + font.getStringWidth(displayText) / 2f, y + heightVal / 2 - font.getHeight() / 2f, moduleAnimation.getOutput().floatValue());
                    }
                    alphaAnimation = moduleAnimation.getOutput().floatValue();
                    break;
            }


            int index = (int) (count * colorIndex.get());
            Pair<Color, Color> colors = HUDMod.getClientColors();

            Color textcolor = ColorUtil.interpolateColorsBackAndForth(colorSpeed.get().intValue(), index, colors.getFirst(), colors.getSecond(), false);

            if (HUDMod.isRainbowTheme()) {
                textcolor = ColorUtil.rainbow(colorSpeed.get().intValue(), index, HUDMod.color1.getRainbow().getSaturation(), 1, 1);
            }


            if (background.get()) {
                float offset = minecraftFont.get() ? 4 : 5;
                Color color = backgroundColor.get() ? textcolor : new Color(10, 10, 10);
                Gui.drawRect2(x - 2, y, font.getStringWidth(displayText) + offset, heightVal,
                        ColorUtil.applyOpacity(color, backgroundAlpha.get().floatValue() * alphaAnimation).getRGB());
            }

            float offset = minecraftFont.get() ? 1 : 0;
            switch (rectangle.get()) {
                default:
                    break;
                case "Top":
                    if (count == 0) {
                        Gui.drawRect2(x - 2, y - 1, textWidth + 5 - offset, 1, textcolor.getRGB());
                    }
                    break;
                case "Side":
                    if (flip) {
                        Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x + textWidth + 2, y, 1, heightVal, textcolor.getRGB());
                    }
                    break;
                case "Outline":
                    if (count != 0) {
                        String modText = applyText(HUDMod.get(Localization.get(lastModule.getName()) + (lastModule.hasMode() ? " " + lastModule.getSuffix() : "")));
                        float texWidth = font.getStringWidth(modText) - textWidth;
                        //Draws the difference of width rect and also the rect on the side of the text
                        if (flip) {
                            Gui.drawRect2(x + textWidth + 3, y, 1, heightVal, textcolor.getRGB());
                            Gui.drawRect2(x + textWidth + 3, y, texWidth + 1, 1, textcolor.getRGB());
                        } else {
                            Gui.drawRect2(x - (3 + texWidth), y, texWidth + 1, 1, textcolor.getRGB());
                            Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                        }
                        if (count == (lastCount - 1)) {
                            Gui.drawRect2(x - 3, y + heightVal, textWidth + 6, 1, textcolor.getRGB());
                        }
                    } else {
                        //Draws the rects for the first module in the count
                        if (flip) {
                            Gui.drawRect2(x + textWidth + 3, y, 1, heightVal, textcolor.getRGB());
                        } else {
                            Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                        }

                        //Top Bar rect
                        Gui.drawRect2(x - 3, y - 1, textWidth + 6, 1, textcolor.getRGB());
                    }
                    //sidebar
                    if (flip) {
                        Gui.drawRect2(x - 3, y, 1, heightVal, textcolor.getRGB());
                    } else {
                        Gui.drawRect2(x + textWidth + 2, y, 1, heightVal, textcolor.getRGB());
                    }


                    break;
            }


            float textYOffset = minecraftFont.get() ? .5f : 0;
            y += textYOffset;
            Color color = ColorUtil.applyOpacity(textcolor, alphaAnimation);
            switch (textShadow.get()) {
                case "None":
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
                case "Colored":
                    RenderUtil.resetColor();
                    font.drawString(StringUtils.stripColorCodes(displayText), x + 1, y + font.getMiddleOfBox(heightVal) + 1, ColorUtil.darker(color, .5f).getRGB());
                    RenderUtil.resetColor();
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
                case "Black":
                    RenderUtil.resetColor();
                    float f = minecraftFont.get() ? 1 : .5f;
                    font.drawString(StringUtils.stripColorCodes(displayText), x + f, y + font.getMiddleOfBox(heightVal) + f,
                            ColorUtil.applyOpacity(Color.BLACK, alphaAnimation));
                    RenderUtil.resetColor();
                    font.drawString(displayText, x, y + font.getMiddleOfBox(heightVal), color.getRGB());
                    break;
            }


            //  font.drawString(displayText, x, (y - 3) + font.getMiddleOfBox(heightVal), color.getRGB());

            if (animation.is("Scale in") && !moduleAnimation.isDone()) {
                RenderUtil.scaleEnd();
            }

            lastModule = module;

            yOffset += animation.is("None") ? 1 * heightVal : moduleAnimation.getOutput().floatValue() * heightVal;
            count++;
        }
        lastCount = count;
        longest = longestModule;
    }

    private String applyText(String text) {
        if (minecraftFont.get() && fontSettings.getSetting("Bold").get()) {
            return "§l" + text.replace("§7", "§7§l");
        }
        return text;
    }


    private AbstractFontRenderer getFont() {
        boolean smallFont = fontSettings.getSetting("Small Font").get();
        if (minecraftFont.get()) {
            return mc.fontRendererObj;
        }

        if (fontSettings.getSetting("Bold").get()) {
            if (smallFont) {
                return duckSansBoldFont18;
            }
            return duckSansBoldFont20;
        }

        return smallFont ? duckSansFont18 : duckSansFont20;
    }

}
