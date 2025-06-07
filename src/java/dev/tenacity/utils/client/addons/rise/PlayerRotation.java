package dev.tenacity.utils.client.addons.rise;

import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.addons.vector.Vec3;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class PlayerRotation implements Utils {
    public static float getYaw(BlockPos pos) {
        return getYaw(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getYaw(AbstractClientPlayer from, Vec3 pos) {
        return from.getRotationYawHead() +
                MathHelper.wrapAngleTo180_float(
                        (float) Math.toDegrees(Math.atan2(pos.getZ() - from.posZ, pos.getX() - from.posX)) - 90f - from.getRotationYawHead()
                );
    }

    public static float getYaw(Vec3 pos) {
        return getYaw(mc.thePlayer, pos);
    }

    public static float getPitch(BlockPos pos) {
        return getPitch(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getPitch(AbstractClientPlayer from, Vec3 pos) {
        double diffX = pos.getX() - from.posX;
        double diffY = pos.getY() - (from.posY + from.getEyeHeight());
        double diffZ = pos.getZ() - from.posZ;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return from.rotationPitch + MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - from.rotationPitch);
    }

    public static float getPitch(Vec3 pos) {
        return getPitch(mc.thePlayer, pos);
    }
}