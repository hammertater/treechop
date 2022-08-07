package ht.treechop.common.util;

import net.minecraft.core.Direction;

import java.util.EnumMap;

public enum FaceShape {
    DOWN(
            Direction.DOWN,
            new Vector3(0, 0, 0),
            new Vector3(1, 0, 0),
            new Vector3(1, 0, 1),
            new Vector3(0, 0, 1)
    ),
    UP(
            Direction.UP,
            new Vector3(0, 1, 0),
            new Vector3(1, 1, 0),
            new Vector3(1, 1, 1),
            new Vector3(0, 1, 1)
    ),
    NORTH(
            Direction.NORTH,
            new Vector3(0, 0, 0),
            new Vector3(1, 0, 0),
            new Vector3(1, 1, 0),
            new Vector3(0, 1, 0)
    ),
    SOUTH(
            Direction.SOUTH,
            new Vector3(0, 0, 1),
            new Vector3(1, 0, 1),
            new Vector3(1, 1, 1),
            new Vector3(0, 1, 1)
    ),
    WEST(
            Direction.WEST,
            new Vector3(0, 0, 0),
            new Vector3(0, 1, 0),
            new Vector3(0, 1, 1),
            new Vector3(0, 0, 1)
    ),
    EAST(
            Direction.EAST,
            new Vector3(1, 0, 0),
            new Vector3(1, 1, 0),
            new Vector3(1, 1, 1),
            new Vector3(1, 0, 1)
    );

    private final Box3 faceBox;
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

    private final Vector3 corner1;
    private final Vector3 corner3;
    private final Vector3 corner4;
    private final Vector3 corner2;

    FaceShape(Direction direction, Vector3 corner1, Vector3 corner2, Vector3 corner3, Vector3 corner4) {
        this.direction = direction;

        Vector3 depthVector = new Vector3(direction.getStepX(), direction.getStepY(), direction.getStepZ())
                .scale(-0.01);
        Vector3 pushedCorner1 = corner1.add(depthVector);
        this.faceBox = new Box3(pushedCorner1, corner3);

        this.corner1 = corner1.scale(16);
        this.corner2 = corner2.scale(16);
        this.corner3 = corner3.scale(16);
        this.corner4 = corner4.scale(16);
    }

    public static FaceShape get(Direction direction) {
        return fromDirections.get(direction);
    }

    public Box3 getBox() {
        return faceBox;
    }

    public Direction getDirection() {
        return direction;
    }

    public Vector3 getCorner1() {
        return corner1;
    }

    public Vector3 getCorner3() {
        return corner3;
    }

    public Vector3 getCorner4() {
        return corner4;
    }

    public Vector3 getCorner2() {
        return corner2;
    }
}
