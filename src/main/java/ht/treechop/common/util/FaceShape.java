package ht.treechop.common.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.EnumMap;

public enum FaceShape {
    DOWN(
            Direction.DOWN,
            new Vector3d(0, 0, 0),
            new Vector3d(1, 0, 0),
            new Vector3d(1, 0, 1),
            new Vector3d(0, 0, 1)
    ),
    UP(
            Direction.UP,
            new Vector3d(0, 1, 0),
            new Vector3d(1, 1, 0),
            new Vector3d(1, 1, 1),
            new Vector3d(0, 1, 1)
    ),
    NORTH(
            Direction.NORTH,
            new Vector3d(0, 0, 0),
            new Vector3d(1, 0, 0),
            new Vector3d(1, 1, 0),
            new Vector3d(0, 1, 0)
    ),
    SOUTH(
            Direction.SOUTH,
            new Vector3d(0, 0, 1),
            new Vector3d(1, 0, 1),
            new Vector3d(1, 1, 1),
            new Vector3d(0, 1, 1)
    ),
    WEST(
            Direction.WEST,
            new Vector3d(0, 0, 0),
            new Vector3d(0, 1, 0),
            new Vector3d(0, 1, 1),
            new Vector3d(0, 0, 1)
    ),
    EAST(
            Direction.EAST,
            new Vector3d(1, 0, 0),
            new Vector3d(1, 1, 0),
            new Vector3d(1, 1, 1),
            new Vector3d(1, 0, 1)
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

    FaceShape(Direction direction, Vector3d corner1, Vector3d corner2, Vector3d corner3, Vector3d corner4) {
        this.direction = direction;

        Vector3d depthVector = new Vector3d(-(float)direction.getXOffset(), -(float)direction.getYOffset(), -(float)direction.getZOffset());
        Vector3d pushedCorner1 = new Vector3d(corner1.getX() + depthVector.getX(), corner1.getY() + depthVector.getY(), corner1.getZ() + depthVector.getZ());
        this.faceBox = new AxisAlignedBB(
                Math.min(pushedCorner1.getX(), corner3.getX()),
                Math.min(pushedCorner1.getY(), corner3.getY()),
                Math.min(pushedCorner1.getZ(), corner3.getZ()),
                Math.max(pushedCorner1.getX(), corner3.getX()),
                Math.max(pushedCorner1.getY(), corner3.getY()),
                Math.max(pushedCorner1.getZ(), corner3.getZ())
        );

        this.corner1 = vector3dTo3f(corner1.scale(16));
        this.corner2 = vector3dTo3f(corner2.scale(16));
        this.corner3 = vector3dTo3f(corner3.scale(16));
        this.corner4 = vector3dTo3f(corner4.scale(16));
    }

    private Vector3f vector3dTo3f(Vector3d vector3d) {
        return new Vector3f((float)vector3d.getX(), (float)vector3d.getY(), (float)vector3d.getZ());
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
