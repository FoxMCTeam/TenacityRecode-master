package dev.tenacity.utils.client.addons.rise;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.addons.vector.Vec3;
import dev.tenacity.utils.tuples.Pair;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

public class AimSimulator implements Utils {
    public static double xRandom = 0;
    public static double yRandom = 0;
    public static double zRandom = 0;
    public static long lastRandom = System.currentTimeMillis();

    public static Pair<Float, Float> getLegitAim(EntityLivingBase target, EntityPlayerSP player,
                                                 boolean nearest, boolean lazy,
                                                 boolean noise, Pair<Float, Float> noiseRandom, long delay) {
        float yaw, pitch;

        final double yDiff = target.posY - player.posY;
        Vec3 targetPosition;
        Vec3 targetEyePosition = new Vec3(target.prevPosX, target.prevPosY, target.prevPosZ).add(new Vec3(0, target.getEyeHeight() - 0.11, 0));
        AxisAlignedBB targetBox = target.getEntityBoundingBox();
        if (yDiff >= 0 && lazy) {
            if (targetEyePosition.getY() - yDiff > target.posY) {
                targetPosition = new Vec3(targetEyePosition.getX(), targetEyePosition.getY() - yDiff, targetEyePosition.getZ());
            } else {
                targetPosition = new Vec3(target.posX, target.posY + 0.4, target.posZ);
            }
        } else {
            targetPosition = nearest ?
                    new Vec3(RotationUtils.getNearestPoint(targetBox, getEyePos()))
                    : targetEyePosition;
        }

        if (noise) {
            if (System.currentTimeMillis() - lastRandom >= delay) {
                xRandom = random(noiseRandom.getFirst());
                yRandom = random(noiseRandom.getSecond());
                zRandom = random(noiseRandom.getFirst());
                lastRandom = System.currentTimeMillis();
            }

            targetPosition.xCoord = normal(targetBox.maxX, targetBox.minX, targetPosition.getX() + xRandom);
            targetPosition.yCoord = normal(targetBox.maxY, targetBox.minY, targetPosition.getY() + yRandom);
            targetPosition.zCoord = normal(targetBox.maxZ, targetBox.minZ, targetPosition.getZ() + zRandom);
        }

        yaw = PlayerRotation.getYaw(targetPosition);
        pitch = PlayerRotation.getPitch(targetPosition);

        return Pair.of(yaw, pitch);
    }

    private static float random(double multiple) {
        return (float) ((Math.random() - 0.5) * 2 * multiple);
    }

    private static double normal(double max, double min, double current) {
        if (current >= max) return max;
        return Math.max(current, min);
    }

    public static float rotMove(float target, float current, float diff) {
        diff *= (float) Math.min(Math.random() + diff * 0.2, diff);

        return rotMoveNoRandom(target, current, diff);
    }

    public static float rotMoveNoRandom(float target, float current, float diff) {
        if (target > current)
            if (target - current > diff)
                return current + diff;
            else
                return target;
        else if (current - target > diff)
            return current - diff;
        else
            return target;
    }

    public static Vec3 getEyePos(Entity entity, Vec3 position) {
        return position.add(new Vec3(0, entity.getEyeHeight(), 0));
    }

    public static Vec3 getEyePos() {
        return getEyePos(mc.thePlayer, new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
    }

}