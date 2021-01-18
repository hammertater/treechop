package ht.treechop.common.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3f;

import java.util.EnumMap;

public enum FaceShape {
    DOWN(
            Direction.DOWN,
            new Vector3f(0, 0, 0),
            new Vector3f(1, 0, 0),
            new Vector3f(1, 0, 1),
            new Vector3f(0, 0, 1)
    ),
    UP(
            Direction.UP,
            new Vector3f(0, 1, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, 1, 1),
            new Vector3f(0, 1, 1)
    ),
    NORTH(
            Direction.NORTH,
            new Vector3f(0, 0, 0),
            new Vector3f(1, 0, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(0, 1, 0)
    ),
    SOUTH(
            Direction.SOUTH,
            new Vector3f(0, 0, 1),
            new Vector3f(1, 0, 1),
            new Vector3f(1, 1, 1),
            new Vector3f(0, 1, 1)
    ),
    WEST(
            Direction.WEST,
            new Vector3f(0, 0, 0),
            new Vector3f(0, 1, 0),
            new Vector3f(0, 1, 1),
            new Vector3f(0, 0, 1)
    ),
    EAST(
            Direction.EAST,
            new Vector3f(1, 0, 0),
            new Vector3f(1, 1, 0),
            new Vector3f(1, 1, 1),
            new Vector3f(1, 0, 1)
    );

    private final AxisAlignedBB faceBox;
    private final Direction direction;

    private static final EnumMap<Direction, FaceShape> fromDirections = new EnumMap<>(Direction.class);

    static {
        fromDirections.put(Direction.DOWN, DOWN);
        fromDirections.put(Direction.UP, UP);
        fromDirections.put(Direction.NORTH, NORTH);
        fromDirections.put(Direction.SOUTH, SOUTH);
        fromDirections.put(Direction.WEST, WEST);
        fromDirections.put(Direction.EAST, EAST);
    }

    private final Vector3f corner1;
    private final Vector3f corner3;
    private final Vector3f corner4;
    private final Vector3f corner2;

    FaceShape(Direction direction, Vector3f corner1, Vector3f corner2, Vector3f corner3, Vector3f corner4) {
        this.direction = direction;

        Vector3f depthVector = direction.toVector3f();
        depthVector.mul(-0.01F);
        Vector3f pushedCorner1 = corner1.copy();
        pushedCorner1.add(depthVector);
        this.faceBox = new AxisAlignedBB(
                Math.min(pushedCorner1.getX(), corner3.getX()),
                Math.min(pushedCorner1.getY(), corner3.getY()),
                Math.min(pushedCorner1.getZ(), corner3.getZ()),
                Math.max(pushedCorner1.getX(), corner3.getX()),
                Math.max(pushedCorner1.getY(), corner3.getY()),
                Math.max(pushedCorner1.getZ(), corner3.getZ())
        );

        this.corner1 = corner1;
        this.corner2 = corner2;
        this.corner3 = corner3;
        this.corner4 = corner4;

        corner1.mul(16);
        corner2.mul(16);
        corner3.mul(16);
        corner4.mul(16);
    }

    public static FaceShape get(Direction direction) {
        return fromDirections.get(direction);
    }

    public AxisAlignedBB getBox() {
        return faceBox;
    }

    public Direction getDirection() {
        return direction;
    }

    public Vector3f getCorner1() {
        return corner1;
    }

    public Vector3f getCorner3() {
        return corner3;
    }

    public Vector3f getCorner4() {
        return corner4;
    }

    public Vector3f getCorner2() {
        return corner2;
    }
}
