package ht.treechop.common.util;

import net.minecraft.dispenser.IPosition;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class Vector3 implements IPosition {

    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    private final double x;
    private final double y;
    private final double z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3f vector3f) {
        this(vector3f.getX(), vector3f.getY(), vector3f.getZ());
    }

    public Vector3(IPosition position) {
        this(position.getX(), position.getY(), position.getZ());
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

    public Vector3 withX(double x) {
        return new Vector3(x, this.getY(), this.getZ());
    }

    public Vector3 withX(Number x) {
        return withX(x.doubleValue());
    }

    public Vector3 withY(double y) {
        return new Vector3(this.getX(), y, this.getZ());
    }

    public Vector3 withY(Number y) {
        return withY(y.doubleValue());
    }

    public Vector3 withZ(double z) {
        return new Vector3(this.getX(), this.getY(), z);
    }

    public Vector3 withZ(Number z) {
        return withZ(z.doubleValue());
    }

    public Vector3 scale(double factor) {
        return new Vector3(this.getX() * factor, this.getY() * factor, this.getZ() * factor);
    }

    public Vector3 scale(Number factor) {
        return scale(factor.doubleValue());
    }

    public Vector3 add(Vector3 vector3) {
        return new Vector3(
                this.getX() + vector3.getX(),
                this.getY() + vector3.getY(),
                this.getZ() + vector3.getZ()
        );
    }

    public Vector3 add(double amount) {
        return new Vector3(
                this.getX() + amount,
                this.getY() + amount,
                this.getZ() + amount
        );
    }

    public Vector3 add(Number amount) {
        return add(amount.doubleValue());
    }

    public Vector3f asVector3f() {
        return new Vector3f((float)this.getX(), (float)this.getY(), (float)this.getZ());
    }

    public Vector3d asVector3d() {
        return new Vector3d(this.getX(), this.getY(), this.getZ());
    }

}
