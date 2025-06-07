package dev.tenacity.utils.client.addons.vector;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.RandomUtils;

import java.util.concurrent.ThreadLocalRandom;

import static dev.tenacity.utils.Utils.mc;

@Setter
@Getter
public class Rotation {
    public double distanceSq;
    float yaw;
    float pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static float updateRotation(float current, float calc, float maxDelta) {
        float f = MathHelper.wrapAngleTo180_float(calc - current);
        if (f > maxDelta) {
            f = maxDelta;
        }
        if (f < -maxDelta) {
            f = -maxDelta;
        }
        return current + f;
    }

    public void apply() {
        mc.thePlayer.rotationYaw = this.yaw;
        mc.thePlayer.rotationPitch = this.pitch;
    }

    public Vector2f toVec2f() {
        return new Vector2f(this.yaw, this.pitch);
    }

    public void toPlayer(EntityPlayer player) {
        if (Float.isNaN(this.yaw) || Float.isNaN(this.pitch)) {
            return;
        }
        this.fixedSensitivity(Float.valueOf(mc.gameSettings.mouseSensitivity));
        player.rotationYaw = this.yaw;
        player.rotationPitch = this.pitch;
    }

    public void fixedSensitivity(Float sensitivity) {
        float f = sensitivity.floatValue() * 0.6f + 0.2f;
        float gcd = f * f * f * 1.2f;
        this.yaw -= this.yaw % gcd;
        this.pitch -= this.pitch % gcd;
    }

    public float rotateToYaw(float yawSpeed, float currentYaw, float calcYaw) {
        float yaw = Rotation.updateRotation(currentYaw, calcYaw, yawSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        double diffYaw = MathHelper.wrapAngleTo180_float(calcYaw - currentYaw);
        if ((double) (-yawSpeed) > diffYaw || diffYaw > (double) yawSpeed) {
            yaw += (float) ((double) RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double) mc.thePlayer.rotationPitch * Math.PI));
        }
        if (yaw == currentYaw) {
            return currentYaw;
        }
        if ((double) mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float f2 = f1 * f1 * f1 * 8.0f;
        int deltaX = (int) ((6.667 * (double) yaw - 6.666666666666667 * (double) currentYaw) / (double) f2);
        float f3 = (float) deltaX * f2;
        yaw = (float) ((double) currentYaw + (double) f3 * 0.15);
        return yaw;
    }

    public float rotateToYaw(float yawSpeed, float[] currentRots, float calcYaw) {
        float yaw = Rotation.updateRotation(currentRots[0], calcYaw, yawSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        if (yaw != calcYaw) {
            yaw += (float) ((double) RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double) currentRots[1] * Math.PI));
        }
        if (yaw == currentRots[0]) {
            return currentRots[0];
        }
        yaw += (float) (ThreadLocalRandom.current().nextGaussian() * 0.2);
        if ((double) mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float f2 = f1 * f1 * f1 * 8.0f;
        int deltaX = (int) ((6.667 * (double) yaw - 6.6666667 * (double) currentRots[0]) / (double) f2);
        float f3 = (float) deltaX * f2;
        yaw = (float) ((double) currentRots[0] + (double) f3 * 0.15);
        return yaw;
    }

    public float rotateToPitch(float pitchSpeed, float currentPitch, float calcPitch) {
        float pitch = Rotation.updateRotation(currentPitch, calcPitch, pitchSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        if (pitch != calcPitch) {
            pitch += (float) ((double) RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double) mc.thePlayer.rotationYaw * Math.PI));
        }
        if ((double) mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float f2 = f1 * f1 * f1 * 8.0f;
        int deltaY = (int) ((6.667 * (double) pitch - 6.666667 * (double) currentPitch) / (double) f2) * -1;
        float f3 = (float) deltaY * f2;
        float f4 = (float) ((double) currentPitch - (double) f3 * 0.15);
        pitch = MathHelper.clamp_float(f4, -90.0f, 90.0f);
        return pitch;
    }

    public float rotateToPitch(float pitchSpeed, float[] currentRots, float calcPitch) {
        float pitch = Rotation.updateRotation(currentRots[1], calcPitch, pitchSpeed + RandomUtils.nextFloat(0.0f, 15.0f));
        if (pitch != calcPitch) {
            pitch += (float) ((double) RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double) currentRots[0] * Math.PI));
        }
        if ((double) mc.gameSettings.mouseSensitivity == 0.5) {
            mc.gameSettings.mouseSensitivity = 0.47887325f;
        }
        float f1 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float f2 = f1 * f1 * f1 * 8.0f;
        int deltaY = (int) ((6.667 * (double) pitch - 6.666667 * (double) currentRots[1]) / (double) f2) * -1;
        float f3 = (float) deltaY * f2;
        float f4 = (float) ((double) currentRots[1] - (double) f3 * 0.15);
        pitch = MathHelper.clamp_float(f4, -90.0f, 90.0f);
        return pitch;
    }

}

