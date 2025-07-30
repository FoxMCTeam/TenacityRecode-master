package dev.tenacity.module.settings.impl;

import com.google.gson.JsonObject;
import dev.tenacity.module.settings.Setting;
import dev.tenacity.utils.render.ColorUtil;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class ColorSetting extends Setting {
    private float hue = 0;
    private float saturation = 1;
    private float brightness = 1;
    private Rainbow rainbow = null;

    public ColorSetting(String name, Color defaultColor) {
        this.name = name;
        this.set(defaultColor);
    }


    public int getColorInt() {
        return get().getRGB();
    }

    public Color get() {
        return isRainbow() ? getRainbow().getColor() : Color.getHSBColor(hue, saturation, brightness);
    }

    public Color getColor() {
        return isRainbow() ? getRainbow().getColor() : Color.getHSBColor(hue, saturation, brightness);
    }

    public void setColorInt(int colorInt) {
        set(new Color(colorInt));
    }

    public void set(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    public void setColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
    }

    public Color getAltColor() {
        return isRainbow() ? getRainbow().getColor(40) : ColorUtil.darker(get(), .6f);
    }

    public void setColor(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }


    public double getHue() {
        return hue;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getBrightness() {
        return brightness;
    }

    public String getHexCode() {
        Color color = get();
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public boolean isRainbow() {
        return rainbow != null;
    }

    public void setRainbow(boolean rainbow) {
        if (rainbow) {
            this.rainbow = new Rainbow();
        } else {
            this.rainbow = null;
        }
    }

    @Override
    public Object getConfigValue() {
        return isRainbow() ? getRainbow().getJsonObject() : get().getRGB();
    }

    @Setter
    public static class Rainbow {
        private float saturation = 1;
        @Getter
        private int speed = 15;


        public Color getColor() {
            return getColor(0);
        }


        public Color getColor(int index) {
            return ColorUtil.rainbow(speed, index, saturation, 1, 1);
        }

        public JsonObject getJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("saturation", saturation);
            jsonObject.addProperty("speed", speed);
            return jsonObject;
        }


        public float getSaturation() {
            return Math.max(0, Math.min(1, saturation));
        }
    }

}
