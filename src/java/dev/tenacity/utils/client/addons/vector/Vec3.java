package dev.tenacity.utils.client.addons.vector;

import net.minecraft.util.Vec3i;

public class Vec3 extends net.minecraft.util.Vec3 {
    private final double x;
    private final double y;
    private final double z;

    public Vec3(double x, double y, double z) {
        super(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(net.minecraft.util.Vec3 vec3) {
        this(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public Vec3(Vec3i blockPos) {
        this(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vec3 addVector(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 floor() {
        return new Vec3(Math.floor(this.x), Math.floor(this.y), Math.floor(this.z));
    }

    public double squareDistanceTo(Vec3 v) {
        return Math.pow(v.x - this.x, 2) + Math.pow(v.y - this.y, 2) + Math.pow(v.z - this.z, 2);
    }

    public Vec3 add(Vec3 v) {
        return addVector(v.getX(), v.getY(), v.getZ());
    }

    public net.minecraft.util.Vec3 mc() {
        return new net.minecraft.util.Vec3(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + x + ";" + y + ";" + z + "]";
    }
}