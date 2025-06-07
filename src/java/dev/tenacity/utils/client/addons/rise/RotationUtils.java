package dev.tenacity.utils.client.addons.rise;

import com.google.common.base.Predicates;
import dev.tenacity.utils.Utils;
import dev.tenacity.utils.client.addons.vector.Rotation;
import dev.tenacity.utils.client.addons.vector.Vector2f;
import dev.tenacity.utils.client.addons.vector.Vector3d;
import dev.tenacity.utils.misc.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RotationUtils implements Utils {

    private static final List<Double> xzPercents = Arrays.asList(0.5, 0.4, 0.3, 0.2, 0.1, 0.0, -0.1, -0.2, -0.3, -0.4, -0.5);

    public static Vec3 getNearestPointBB(Vec3 eye, AxisAlignedBB box) {
        double[] origin = new double[]{eye.xCoord, eye.yCoord, eye.zCoord};
        double[] destMins = new double[]{box.minX, box.minY, box.minZ};
        double[] destMaxs = new double[]{box.maxX, box.maxY, box.maxZ};
        for (int i = 0; i < 3; ++i) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
                continue;
            }
            if (!(origin[i] < destMins[i])) continue;
            origin[i] = destMins[i];
        }
        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static Rotation rotationToFace(BlockPos targetPos, EnumFacing targetFace, Vec3 helpVector) {
        AxisAlignedBB bb = mc.theWorld.getBlockState(targetPos).getBlock().getCollisionBoundingBox(mc.theWorld, targetPos, mc.theWorld.getBlockState(targetPos));
        double height = bb.maxY - bb.minY;
        double xWidth = bb.maxX - bb.minX;
        double zWidth = bb.maxZ - bb.minZ;
        Vec3 hitVec = new Vec3(bb.minX, bb.minY, bb.minZ).add(new Vec3(xWidth / 2f, height / 2f, zWidth / 2f));
        Vec3i faceVec = targetFace.getDirectionVec();
        Vec3 directionVec = new Vec3(faceVec.getX() * (xWidth / 2f), faceVec.getY() * (height / 2f), faceVec.getZ() * (zWidth / 2f));
        hitVec = hitVec.add(directionVec);
        double max = 0.4;
        double fixX = 0.0;
        double fixZ = 0.0;
        double fixY = 0.0;
        if (helpVector != null) {
            if (directionVec.getX() == 0) {
                fixX += Math.min(-xWidth / 2f * max, Math.max(xWidth / 2f * max, helpVector.getX() - hitVec.getX()));
            }
            if (directionVec.getY() == 0) {
                fixY += Math.min(-height / 2f * max, Math.max(height / 2f * max, helpVector.getY() - hitVec.getY()));
            }
            if (directionVec.getZ() == 0) {
                fixZ += Math.min(-zWidth / 2f * max, Math.max(zWidth / 2f * max, helpVector.getZ() - hitVec.getZ()));
            }
        }
        hitVec = hitVec.add(new Vec3(fixX, fixY, fixZ));
        return toRotation(hitVec);
    }

    public static Vec3 getVectorForRotation(float yaw, float pitch) {
        // 将角度转为弧度
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        // 计算单位方向向量的各个分量
        float f = MathHelper.cos(-yawRad - (float) Math.PI);
        float f1 = MathHelper.sin(-yawRad - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitchRad);
        float f3 = MathHelper.sin(-pitchRad);

        // 返回方向向量（x, y, z）
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static Vec3 getNearestPoint(AxisAlignedBB from, Vec3 to) {
        double pointX, pointY, pointZ;
        if (to.xCoord >= from.maxX) {
            pointX = from.maxX;
        } else pointX = Math.max(to.xCoord, from.minX);
        if (to.yCoord >= from.maxY) {
            pointY = from.maxY;
        } else pointY = Math.max(to.yCoord, from.minY);
        if (to.zCoord >= from.maxZ) {
            pointZ = from.maxZ;
        } else pointZ = Math.max(to.zCoord, from.minZ);

        return new Vec3(pointX, pointY, pointZ);
    }

    public static double getRotationDifference(Entity entity) {
        Vector2f rotation = toRotation(getCenter(entity.getEntityBoundingBox()), true);
        return getRotationDifference(rotation, new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));
    }

    public static float getAngleDifference(float a, float b2) {
        return ((a - b2) % 360.0f + 540.0f) % 360.0f - 180.0f;
    }

    public static double getRotationDifference(Vector2f a, Vector2f b2) {
        return Math.hypot(getAngleDifference(a.getX(), b2.getX()), a.getY() - b2.getY());
    }

    public static Vec3 getCenter(AxisAlignedBB bb) {
        return new Vec3(bb.minX + (bb.maxX - bb.minX) * 0.5, bb.minY + (bb.maxY - bb.minY) * 0.5, bb.minZ + (bb.maxZ - bb.minZ) * 0.5);
    }

    public static float adjustAngle(float currentAngle, float targetAngle, float maxStep) {
        float delta = MathHelper.wrapAngleTo180_float(targetAngle - currentAngle);
        if (delta > maxStep) {
            delta = maxStep;
        }

        if (delta < -maxStep) {
            delta = -maxStep;
        }

        return currentAngle + delta;
    }

    public static Rotation toRotation(Vec3 eyesPos, Vec3 targetVec) {
        double xDiff = targetVec.xCoord - eyesPos.xCoord;
        double zDiff = targetVec.zCoord - eyesPos.zCoord;
        double yDiff = targetVec.yCoord - eyesPos.yCoord;
        double xzDistance = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = adjustAngle(0.0F, (float) (Math.atan2(zDiff, xDiff) * 180.0 / Math.PI) - 90.0F, 360.0F);
        float pitch = adjustAngle(mc.thePlayer.rotationPitch, (float) (-(Math.atan2(yDiff, xzDistance) * 180.0 / Math.PI)), 360.0F);
        return new Rotation(yaw, pitch);
    }


    public static Rotation toRotation(Vec3 targetVec) {
        return toRotation(mc.thePlayer.getPositionEyes(1f), targetVec);
    }

    public static Vector2f toRotation(Vec3 vec, boolean predict) {
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        if (predict) {
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        }
        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;
        return new Vector2f(MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f), MathHelper.wrapAngleTo180_float((float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))));
    }

    public static Vec3 getVec(Entity entity) {
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }

    public static Vector2f calculateSimple(Entity entity, double range, double wallRange) {
        AxisAlignedBB aabb = entity.getEntityBoundingBox().contract(-0.05, -0.05, -0.05).contract(0.05, 0.05, 0.05);
        range += 0.05;
        wallRange += 0.05;
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 nearest = new Vec3(MathUtils.clamp(eyePos.xCoord, aabb.minX, aabb.maxX), MathUtils.clamp(eyePos.yCoord, aabb.minY, aabb.maxY), MathUtils.clamp(eyePos.zCoord, aabb.minZ, aabb.maxZ));
        Vector2f rotation = toRotation(nearest, false);
        if (nearest.subtract(eyePos).lengthSquared() <= wallRange * wallRange) {
            return rotation;
        }
        MovingObjectPosition result = RayCastUtil.rayCast(rotation, range, 0.0f, false);
        double maxRange = Math.max(wallRange, range);
        if (result != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && result.entityHit == entity && result.hitVec.subtract(eyePos).lengthSquared() <= maxRange * maxRange) {
            return rotation;
        }
        return null;
    }

    public static Rotation toRotationRot(Vec3 vec, boolean predict) {
        Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        if (predict) {
            eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        }
        double diffX = vec.xCoord - eyesPos.xCoord;
        double diffY = vec.yCoord - eyesPos.yCoord;
        double diffZ = vec.zCoord - eyesPos.zCoord;
        return new Rotation(MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f), MathHelper.wrapAngleTo180_float((float) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))));
    }

    public static Vector2f getRotationFromEyeToPoint(Vector3d point3d) {
        return calculate(new Vector3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), point3d);
    }

    public static Vector2f getRotations(Entity entity) {
        double pX = Minecraft.getMinecraft().thePlayer.posX;
        double pY = Minecraft.getMinecraft().thePlayer.posY + (double) Minecraft.getMinecraft().thePlayer.getEyeHeight();
        double pZ = Minecraft.getMinecraft().thePlayer.posZ;
        double eX = entity.posX;
        double eY = entity.posY + (double) (entity.height / 2.0f);
        double eZ = entity.posZ;
        double dX = pX - eX;
        double dY = pY - eY;
        double dZ = pZ - eZ;
        double dH = Math.sqrt(Math.pow(dX, 2.0) + Math.pow(dZ, 2.0));
        double yaw = Math.toDegrees(Math.atan2(dZ, dX)) + 90.0;
        double pitch = Math.toDegrees(Math.atan2(dH, dY));
        return new Vector2f((float) yaw, (float) (90.0 - pitch));
    }

    public static Vector2f calculate(Vector3d from, Vector3d to) {
        double x = to.getX() - from.getX();
        double y = to.getY() - from.getY();
        double z = to.getZ() - from.getZ();
        double sqrt = Math.sqrt(x * x + z * z);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, sqrt)));
        return new Vector2f(yaw, Math.min(Math.max(pitch, -90.0f), 90.0f));
    }

    public static Vector2f calculate(Entity entity, boolean adaptive, double range, double wallRange, boolean predict, boolean randomCenter) {
        MovingObjectPosition normalResult;
        if (mc.thePlayer == null) {
            return null;
        }
        double rangeSq = range * range;
        double wallRangeSq = wallRange * wallRange;
        Vector2f simpleRotation = calculateSimple(entity, range, wallRange);
        if (simpleRotation != null) {
            return simpleRotation;
        }
        Vector2f normalRotations = toRotation(getVec(entity), predict);
        if (!randomCenter && (normalResult = RayCastUtil.rayCast(normalRotations, range, 0.0f, false)) != null && normalResult.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            return normalRotations;
        }
        double yStart = 1.0;
        double yEnd = 0.0;
        double yStep = -0.5;
        if (randomCenter && MathUtils.secureRandom.nextBoolean()) {
            yStart = 0.0;
            yEnd = 1.0;
            yStep = 0.5;
        }
        double yPercent = yStart;
        while (Math.abs(yEnd - yPercent) > 0.001) {
            double xzStart = 0.5;
            double xzEnd = -0.5;
            double xzStep = -0.1;
            if (randomCenter) {
                Collections.shuffle(xzPercents);
            }
            for (double xzPercent : xzPercents) {
                for (int side = 0; side <= 3; ++side) {
                    MovingObjectPosition result;
                    double xPercent = 0.0;
                    double zPercent = 0.0;
                    switch (side) {
                        case 0: {
                            xPercent = xzPercent;
                            zPercent = 0.5;
                            break;
                        }
                        case 1: {
                            xPercent = xzPercent;
                            zPercent = -0.5;
                            break;
                        }
                        case 2: {
                            xPercent = 0.5;
                            zPercent = xzPercent;
                            break;
                        }
                        case 3: {
                            xPercent = -0.5;
                            zPercent = xzPercent;
                        }
                    }
                    Vec3 Vec32 = getVec(entity).add(new Vec3((entity.getEntityBoundingBox().maxX - entity.getEntityBoundingBox().minX) * xPercent, (entity.getEntityBoundingBox().maxY - entity.getEntityBoundingBox().minY) * yPercent, (entity.getEntityBoundingBox().maxZ - entity.getEntityBoundingBox().minZ) * zPercent));
                    double distanceSq = Vec32.squareDistanceTo(mc.thePlayer.getPositionEyes(1.0f));
                    Rotation rotation = toRotationRot(Vec32, predict);
                    rotation.fixedSensitivity(Float.valueOf(mc.gameSettings.mouseSensitivity));
                    rotation.distanceSq = distanceSq;
                    if (distanceSq <= wallRangeSq && (result = RayCastUtil.rayCast(rotation.toVec2f(), wallRange, 0.0f, true)) != null && result.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        return rotation.toVec2f();
                    }
                    if (!(distanceSq <= rangeSq) || (result = RayCastUtil.rayCast(rotation.toVec2f(), range, 0.0f, false)) == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY)
                        continue;
                    return rotation.toVec2f();
                }
            }
            yPercent += yStep;
        }
        return null;
    }

    public static float[] getThrowRotation(final Entity entity) {
        if (entity == null) {
            return null;
        }
        Minecraft mc = Minecraft.getMinecraft();
        final double xSize = entity.posX - mc.thePlayer.posX - mc.thePlayer.motionX;
        final double ySize = entity.posY + entity.getEyeHeight() + 0.3 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double zSize = entity.posZ - mc.thePlayer.posZ - mc.thePlayer.motionZ;
        final double theta = MathHelper.sqrt_double(xSize * xSize + zSize * zSize);
        final float yaw = (float) (Math.atan2(zSize, xSize) * 180 / Math.PI) - 90;
        final float pitch = (float) (-(Math.atan2(ySize, theta) * 180 / Math.PI));
        return new float[]{(mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw)) % 360, (mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)) % 360.0f};
    }

    public static float[] getHVHRotation(final Entity entity) {
        if (entity == null) {
            return null;
        }
        double diffX = entity.posX - mc.thePlayer.posX;
        double diffZ = entity.posZ - mc.thePlayer.posZ;
        Vec3 BestPos = getNearestPointBB(mc.thePlayer.getPositionEyes(1.0f), entity.getEntityBoundingBox());
        Location myEyePos = new Location(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), Minecraft.getMinecraft().thePlayer.posZ);
        double diffY = BestPos.yCoord - myEyePos.getY();
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotationsNeeded(final Entity entity) {
        if (entity == null) {
            return null;
        }
        Minecraft mc = Minecraft.getMinecraft();
        final double xSize = entity.posX - mc.thePlayer.posX - mc.thePlayer.motionX;
        final double ySize = entity.getEntityBoundingBox().minY + entity.getEyeHeight() - mc.thePlayer.motionY - 0.1 - (mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight());
        final double zSize = entity.posZ - mc.thePlayer.posZ - mc.thePlayer.motionZ;
        final double theta = MathHelper.sqrt_double(xSize * xSize + zSize * zSize);
        final float yaw = (float) (Math.atan2(zSize, xSize) * 180 / Math.PI) - 90;
        final float pitch = (float) (-(Math.atan2(ySize, theta) * 180 / Math.PI));
        return new float[]{(mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw)) % 360, (mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)) % 360.0f};
    }

    public static float getYaw(Vec3 to) {
        float x = (float) (to.xCoord - mc.thePlayer.posX);
        float z = (float) (to.zCoord - mc.thePlayer.posZ);
        float var1 = (float) (StrictMath.atan2(z, x) * 180.0D / StrictMath.PI) - 90.0F;
        float rotationYaw = mc.thePlayer.rotationYaw;
        return rotationYaw + MathHelper.wrapAngleTo180_float(var1 - rotationYaw);
    }

    public static Vec3 getVecRotations(float yaw, float pitch) {
        double d = Math.cos(Math.toRadians(-yaw) - Math.PI);
        double d1 = Math.sin(Math.toRadians(-yaw) - Math.PI);
        double d2 = -Math.cos(Math.toRadians(-pitch));
        double d3 = Math.sin(Math.toRadians(-pitch));
        return new Vec3(d1 * d2, d3, d * d2);
    }

    public static float[] getRotations(double posX, double posY, double posZ) {
        double x = posX - mc.thePlayer.posX, z = posZ - mc.thePlayer.posZ, y = posY - (mc.thePlayer.getEyeHeight() + mc.thePlayer.posY);
        double d3 = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (MathHelper.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(y, d3) * 180.0D / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static boolean isMouseOver(final float yaw, final float pitch, final Entity target, final float range) {
        final float partialTicks = mc.timer.renderPartialTicks;
        final Entity entity = mc.getRenderViewEntity();
        MovingObjectPosition objectMouseOver;
        Entity mcPointedEntity = null;

        if (entity != null && mc.theWorld != null) {

            mc.mcProfiler.startSection("pick");
            final double d0 = mc.playerController.getBlockReachDistance();
            objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);
            final boolean flag = d0 > (double) range;

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch, yaw);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > (double) range) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mcPointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();

            return mcPointedEntity == target;
        }

        return false;
    }

}