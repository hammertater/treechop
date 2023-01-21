package ht.treechop.common.util;

import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import ht.treechop.client.model.ModelUtil;
import net.minecraft.core.Position;

public class Vector3 implements Position {

    public final double x;
    public final double y;
    public final double z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3f vector3f) {
        this(vector3f.x(), vector3f.y(), vector3f.z());
    }

    public Vector3(Position position) {
        this(position.x(), position.y(), position.z());
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public Vector3 withX(double x) {
        return new Vector3(x, this.y(), this.z());
    }

    public Vector3 withX(Number x) {
        return withX(x.doubleValue());
    }

    public Vector3 withY(double y) {
        return new Vector3(this.x(), y, this.z());
    }

    public Vector3 withY(Number y) {
        return withY(y.doubleValue());
    }

    public Vector3 withZ(double z) {
        return new Vector3(this.x(), this.y(), z);
    }

    public Vector3 withZ(Number z) {
        return withZ(z.doubleValue());
    }

    public Vector3 scale(double factor) {
        return new Vector3(this.x() * factor, this.y() * factor, this.z() * factor);
    }

    public Vector3 scale(Number factor) {
        return scale(factor.doubleValue());
    }

    public Vector3 add(Vector3 vector3) {
        return new Vector3(
                this.x() + vector3.x(),
                this.y() + vector3.y(),
                this.z() + vector3.z()
        );
    }

    public Vector3 add(double amount) {
        return new Vector3(
                this.x() + amount,
                this.y() + amount,
                this.z() + amount
        );
    }

    public Vector3 add(Number amount) {
        return add(amount.doubleValue());
    }

    public Vector3f asVector3f() {
        return new Vector3f((float)this.x(), (float)this.y(), (float)this.z());
    }

    public Vector3d asVector3d() {
        return new Vector3d(this.x(), this.y(), this.z());
    }

    public Vector3 clamp(Vector3 min, Vector3 max) {
        return new Vector3(
                clamp(x, min.x, max.x),
                clamp(y, min.y, max.y),
                clamp(z, min.z, max.z)
        );
    }

    private double clamp(double value, double min, double max) {
        return Double.max(Double.min(value, max), min);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public double length() {
        return Math.sqrt(length2());
    }

    private double length2() {
        return x * x + y * y + z * z;
    }

    public double dot(Vector3 vector) {
        return dot(vector.x, vector.y, vector.z);
    }

    public double dot(double x, double y, double z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public Vector3 cross(Vector3 vector) {
        return cross(vector.x, vector.y, vector.z);
    }

    private Vector3 cross(double x, double y, double z) {
        return new Vector3(
                this.y * z - this.z * y,
                this.z * x - this.x * z,
                this.x * y - this.y * x
        );
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public Vector3 normalize() {
        return scale(1 / length());
    }

}
