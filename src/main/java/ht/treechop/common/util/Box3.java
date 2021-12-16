package ht.treechop.common.util;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.AABB;

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
                Math.min(first.x(), second.x()),
                Math.min(first.y(), second.y()),
                Math.min(first.z(), second.z()),
                Math.max(first.x(), second.x()),
                Math.max(first.y(), second.y()),
                Math.max(first.z(), second.z())
        );
    }

    public Box3(Vector3d first, Vector3d second) {
        this(new Vector3(first.x, first.y, first.z), new Vector3(first.x, first.y, first.z));
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

    public AABB asAxisAlignedBB() {
        return new AABB(
                this.x1,
                this.y1,
                this.z1,
                this.x2,
                this.y2,
                this.z2
        );
    }

}
