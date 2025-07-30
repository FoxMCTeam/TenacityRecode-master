package dev.tenacity.utils.client.addons.smoothscrollingeverywhere;

import lombok.Getter;
import net.minecraft.util.MathHelper;

import java.io.*;
import java.util.Properties;
import java.util.function.Function;

public class SmoothScrollingEverywhere {
    @Getter
    private static Function<Double, Double> easingMethod = v -> v;
    private static float scrollDuration = 600f;
    @Getter
    private static float scrollStep = 19f;
    @Getter
    private static float bounceBackMultiplier = 0.24f;
    @Getter
    private static boolean unlimitFps = true;

    private static final File configFile = new File("config/smoothscroll.cfg");

    static {
        loadConfig();
    }

    // 加载配置
    private static void loadConfig() {
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                saveDefaultConfig();
            }

            Properties props = new Properties();
            try (FileReader reader = new FileReader(configFile)) {
                props.load(reader);
            }

            scrollDuration = Float.parseFloat(props.getProperty("scrollDuration", "600"));
            scrollStep = Float.parseFloat(props.getProperty("scrollStep", "19"));
            bounceBackMultiplier = Float.parseFloat(props.getProperty("bounceBackMultiplier", "0.24"));
            unlimitFps = Boolean.parseBoolean(props.getProperty("unlimitFps", "true"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveDefaultConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("scrollDuration=600\n");
            writer.write("scrollStep=19\n");
            writer.write("bounceBackMultiplier=0.24\n");
            writer.write("unlimitFps=true\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getScrollDuration() {
        return (long) scrollDuration;
    }

    public static float handleScrollingPosition(float[] target, float scroll, float maxScroll, float delta, double start, double duration) {
        if (getBounceBackMultiplier() >= 0) {
            target[0] = clamp(target[0], maxScroll);
            if (target[0] < 0) {
                target[0] -= target[0] * (1 - getBounceBackMultiplier()) * delta / 3;
            } else if (target[0] > maxScroll) {
                target[0] = (target[0] - maxScroll) * (1 - (1 - getBounceBackMultiplier()) * delta / 3) + maxScroll;
            }
        } else {
            target[0] = clamp(target[0], maxScroll, 0);
        }

        if (!Precision.almostEquals(scroll, target[0], Precision.FLOAT_EPSILON)) {
            return expoEase(scroll, target[0], Math.min((System.currentTimeMillis() - start) / duration * delta * 3, 1));
        } else {
            return target[0];
        }
    }

    public static float expoEase(float start, float end, double amount) {
        return start + (end - start) * getEasingMethod().apply(amount).floatValue();
    }

    public static double clamp(double v, double maxScroll) {
        return clamp(v, maxScroll, 300);
    }

    public static double clamp(double v, double maxScroll, double clampExtension) {
        return MathHelper.clamp_double(v, -clampExtension, maxScroll + clampExtension);
    }

    public static float clamp(float v, float maxScroll) {
        return clamp(v, maxScroll, 300);
    }

    public static float clamp(float v, float maxScroll, float clampExtension) {
        return MathHelper.clamp_float(v, -clampExtension, maxScroll + clampExtension);
    }

    private static class Precision {
        public static final float FLOAT_EPSILON = 1e-3f;
        public static final double DOUBLE_EPSILON = 1e-7;

        public static boolean almostEquals(float value1, float value2, float acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }

        public static boolean almostEquals(double value1, double value2, double acceptableDifference) {
            return Math.abs(value1 - value2) <= acceptableDifference;
        }
    }
}
