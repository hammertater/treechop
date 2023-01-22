package ht.treechop.common.util;

public class Vector2 {

    public final double x;
    public final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public Vector2 withX(double x) {
        return new Vector2(x, y);
    }

    public Vector2 withX(Number x) {
        return withX(x.doubleValue());
    }

    public Vector2 withY(double y) {
        return new Vector2(x, y);
    }

    public Vector2 withY(Number y) {
        return withY(y.doubleValue());
    }

    public Vector2 scale(double factor) {
        return new Vector2(x * factor, y * factor);
    }

    public Vector2 scale(Number factor) {
        return scale(factor.doubleValue());
    }

    public Vector2 add(Vector2 vector2) {
        return new Vector2(x + vector2.x, y + vector2.y);
    }

    public Vector2 add(double amount) {
        return new Vector2(x + amount, y + amount);
    }

    public Vector2 add(Number amount) {
        return add(amount.doubleValue());
    }

    public Vector2 clamp(Vector2 min, Vector2 max) {
        return new Vector2(
                clamp(x, min.x, max.x),
                clamp(y, min.y, max.y)
        );
    }

    private double clamp(double value, double min, double max) {
        return Double.max(Double.min(value, max), min);
    }

    public Vector2 subtract(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    public double length() {
        return Math.sqrt(length2());
    }

    private double length2() {
        return x * x + y * y;
    }

    public double dot(double x, double y) {
        return this.x * x + this.y * y;
    }
}
