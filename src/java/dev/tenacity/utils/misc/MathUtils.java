//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft1.12.2 Mappings"!

//Decompiled by Procyon!

package dev.tenacity.utils.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Random;

public class MathUtils {
    public static final DecimalFormat DF_0;
    public static final DecimalFormat DF_1;
    public static final DecimalFormat DF_2;
    public static final DecimalFormat DF_1D;
    public static final DecimalFormat DF_2D;
    public static final SecureRandom secureRandom;
    public static Random random;

    static {
        DF_0 = new DecimalFormat("0");
        DF_1 = new DecimalFormat("0.0");
        DF_2 = new DecimalFormat("0.00");
        DF_1D = new DecimalFormat("0.#");
        DF_2D = new DecimalFormat("0.##");
        secureRandom = new SecureRandom();
        MathUtils.random = new Random();
    }

    public static float scrollSpeed(float yOffset, float speed) {
        return yOffset * speed;
    }

    public static int randomizeInt(double min, double max) {
        return (int) randomizeDouble(min, max);
    }

    public static double randomizeDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    // 定义一个方法，返回当前平滑的值
    public static int getSmoothValue(float max, float min, float speed) {
        // 获取当前系统时间，单位为纳秒，精度更高
        long currentTime = System.nanoTime();

        // 计算经过的时间（秒）
        float timeElapsed = currentTime / 1_000_000_000.0f;

        // 根据时间和速度计算递减后的当前值
        float currentValue = max - (timeElapsed * speed);

        // 保证当前值不会低于最小值
        if (currentValue < min) {
            currentValue = min;
        }

        // 返回当前值的整数部分
        return (int) currentValue;
    }

    public static double clamp(double num, double min, double max) {
        if (num < min) {
            return min;
        } else {
            return Math.min(num, max);
        }
    }

    public static float random(float min, float max) {
        return (float) (Math.random() * (max - min) + min);
    }

    public static double linearInterpolate(final double min, final double max, final double norm) {
        return (max - min) * norm + min;
    }

    public static double roundToDecimalPlace(final double value, final double inc) {
        final double halfOfInc = inc / 2.0;
        final double floored = StrictMath.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(StrictMath.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
        }
        return new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }

    public static float[][] getArcVertices(final float radius, final float angleStart, final float angleEnd, final int segments) {
        final float range = Math.max(angleStart, angleEnd) - Math.min(angleStart, angleEnd);
        final int nSegments = Math.max(2, Math.round(360.0f / range * segments));
        final float segDeg = range / nSegments;
        final float[][] vertices = new float[nSegments + 1][2];
        for (int i = 0; i <= nSegments; ++i) {
            final float angleOfVert = (angleStart + i * segDeg) / 180.0f * 3.1415927f;
            vertices[i][0] = (float) Math.sin(angleOfVert) * radius;
            vertices[i][1] = (float) (-Math.cos(angleOfVert)) * radius;
        }
        return vertices;
    }

    public static int getRandomInRange(final int min, final int max) {
        return (int) (Math.random() * (max - min) + min);
    }

    public static double[] yawPos(final double value) {
        return yawPos(Minecraft.getMinecraft().thePlayer.rotationYaw * MathHelper.deg2Rad, value);
    }

    public static double[] yawPos(final float yaw, final double value) {
        return new double[]{-MathHelper.sin(yaw) * value, MathHelper.cos(yaw) * value};
    }

    public static float getRandomInRange(final float min, final float max) {
        final SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }

    public static double getRandomInRange(final double min, final double max) {
        final SecureRandom random = new SecureRandom();
        return (min == max) ? min : (random.nextDouble() * (max - min) + min);
    }

    public static int getRandomNumberUsingNextInt(final int min, final int max) {
        final Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static double lerp(final double old, final double newVal, final double amount) {
        return (1.0 - amount) * old + amount * newVal;
    }

    public static Double interpolate(final double oldValue, final double newValue, final double interpolationValue) {
        return oldValue + (newValue - oldValue) * interpolationValue;
    }

    public static float interpolateFloat(final float oldValue, final float newValue, final double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).floatValue();
    }

    public static int interpolateInt(final int oldValue, final int newValue, final double interpolationValue) {
        return interpolate(oldValue, newValue, (float) interpolationValue).intValue();
    }

    public static float calculateGaussianValue(final float x, final float sigma) {
        final double output = 1.0 / Math.sqrt(6.283185307179586 * (sigma * sigma));
        return (float) (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma))));
    }

    public static double roundToHalf(final double d) {
        return Math.round(d * 2.0) / 2.0;
    }

    public static double round(final double num, final double increment) {
        BigDecimal bd = new BigDecimal(num);
        bd = bd.setScale((int) increment, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static double round(final double value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String round(final String value, final int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.stripTrailingZeros();
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.toString();
    }

    public static Random getRandom() {
        return MathUtils.random;
    }

    public static float getRandomFloat(final float max, final float min) {
        final SecureRandom random = new SecureRandom();
        return random.nextFloat() * (max - min) + min;
    }

    public static int getRandom(final int min, final int max) {
        if (max < min) {
            return 0;
        }
        return min + MathUtils.random.nextInt(max - min + 1);
    }

    public static long getRandom(final long min, final long max) {
        final long range = max - min;
        long scaled = MathUtils.random.nextLong() * range;
        if (scaled > max) {
            scaled = max;
        }
        long shifted = scaled + min;
        if (shifted > max) {
            shifted = max;
        }
        return shifted;
    }

    public static double getRandom(final double min, final double max) {
        final double range = max - min;
        double scaled = MathUtils.random.nextDouble() * range;
        if (scaled > max) {
            scaled = max;
        }
        double shifted = scaled + min;
        if (shifted > max) {
            shifted = max;
        }
        return shifted;
    }

    public static int getNumberOfDecimalPlace(final double value) {
        final BigDecimal bigDecimal = new BigDecimal(value);
        return Math.max(0, bigDecimal.stripTrailingZeros().scale());
    }
}
