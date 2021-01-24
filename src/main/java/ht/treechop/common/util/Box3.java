package ht.treechop.common.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class Box3 {

    public static final Box3 ZERO = new Box3(0, 0, 0, 0, 0, 0);

    private final double x1;
    private final double y1;
    private final double z1;
    private final double x2;
    private final double y2;
    private final double z2;

    public Box3(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    public Box3(Vector3 first, Vector3 second) {
        this(
                Math.min(first.getX(), second.getX()),
                Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()),
                Math.max(first.getX(), second.getX()),
                Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ())
        );
    }

    public Box3(Vector3d first, Vector3d second) {
        this(new Vector3(first), new Vector3(second));
    }

    public Box3(Vector3f first, Vector3f second) {
        this(new Vector3(first), new Vector3(second));
    }

    public double getMinX() {
        return x1;
    }

    public double getMinY() {
        return y1;
    }

    public double getMinZ() {
        return z1;
    }

    public double getMaxX() {
        return x2;
    }

    public double getMaxY() {
        return y2;
    }

    public double getMaxZ() {
        return z2;
    }

    public AxisAlignedBB asAxisAlignedBB() {
        return new AxisAlignedBB(
                this.x1,
                this.y1,
                this.z1,
                this.x2,
                this.y2,
                this.z2
        );
    }

}
