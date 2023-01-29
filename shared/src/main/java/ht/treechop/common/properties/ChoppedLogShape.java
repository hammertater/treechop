package ht.treechop.common.properties;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.DirectionBitMasks;
import ht.treechop.common.util.FaceShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum ChoppedLogShape implements StringRepresentable {
    PILLAR_Y("pillar", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST), // "pillar" instead of "pillar_y" for backwards compatibility
    CORNER_NWEU("corner_nweu", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_NWED("corner_nwed", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    CORNER_NSWU("corner_nswu", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_NSWD("corner_nswd", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    CORNER_NWUD("corner_nwud", DirectionBitMasks.NORTH | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_SWEU("corner_sweu", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_SWED("corner_swed", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    PILLAR_Z("pillar_z", DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_SWUD("corner_swud", DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_NSEU("corner_nseu", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.UP),
    CORNER_NSED("corner_nsed", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.DOWN),
    CORNER_NEUD("corner_neud", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    PILLAR_X("pillar_x", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_SEUD("corner_seud", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    END_N("end_n", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST),
    END_W("end_w", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.WEST),
    CORNER_NWU("corner_nwu", DirectionBitMasks.NORTH | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_NWD("corner_nwd", DirectionBitMasks.NORTH | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    END_S("end_s", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST),
    CORNER_WEU("corner_weu", DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_WED("corner_wed", DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    CORNER_SWU("corner_swu", DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_SWD("corner_swd", DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    CORNER_WUD("corner_wud", DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    END_E("end_e", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST),
    CORNER_NEU("corner_neu", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.UP),
    CORNER_NED("corner_ned", DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.DOWN),
    END_U("end_u", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.UP),
    END_D("end_d", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.DOWN),
    CORNER_NUD("corner_nud", DirectionBitMasks.NORTH | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_SEU("corner_seu", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.UP),
    CORNER_SED("corner_sed", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.DOWN),
    CORNER_EUD("corner_eud", DirectionBitMasks.EAST | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_SUD("corner_sud", DirectionBitMasks.SOUTH | DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    CORNER_NW("corner_nw", DirectionBitMasks.NORTH | DirectionBitMasks.WEST),
    SLAB_YZ("slab_yz", DirectionBitMasks.EAST | DirectionBitMasks.WEST),
    CORNER_SW("corner_sw", DirectionBitMasks.SOUTH | DirectionBitMasks.WEST),
    CORNER_WU("corner_wu", DirectionBitMasks.WEST | DirectionBitMasks.UP),
    CORNER_WD("corner_wd", DirectionBitMasks.WEST | DirectionBitMasks.DOWN),
    CORNER_NE("corner_ne", DirectionBitMasks.NORTH | DirectionBitMasks.EAST),
    SLAB_XY("slab_xy", DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH),
    CORNER_NU("corner_nu", DirectionBitMasks.NORTH | DirectionBitMasks.UP),
    CORNER_ND("corner_nd", DirectionBitMasks.NORTH | DirectionBitMasks.DOWN),
    CORNER_SE("corner_se", DirectionBitMasks.SOUTH | DirectionBitMasks.EAST),
    CORNER_EU("corner_eu", DirectionBitMasks.EAST | DirectionBitMasks.UP),
    CORNER_ED("corner_ed", DirectionBitMasks.EAST | DirectionBitMasks.DOWN),
    CORNER_SU("corner_su", DirectionBitMasks.SOUTH | DirectionBitMasks.UP),
    CORNER_SD("corner_sd", DirectionBitMasks.SOUTH | DirectionBitMasks.DOWN),
    SLAB_XZ("slab_xz", DirectionBitMasks.UP | DirectionBitMasks.DOWN),
    SIDE_W("side_w", DirectionBitMasks.WEST),
    SIDE_N("side_n", DirectionBitMasks.NORTH),
    SIDE_E("side_e", DirectionBitMasks.EAST),
    SIDE_S("side_s", DirectionBitMasks.SOUTH),
    SIDE_U("side_u", DirectionBitMasks.UP),
    SIDE_D("side_d", DirectionBitMasks.DOWN);

    private static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 8;

    private final String name;
    private final byte openSides;
    private final Map<Integer, AABB> radiusBoxes;

    private static final ChoppedLogShape[] openSidesMap
            = new ChoppedLogShape[(DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN) + 1];

    static {
        Arrays.stream(ChoppedLogShape.values()).forEach(shape -> openSidesMap[shape.openSides] = shape);
        openSidesMap[0] = PILLAR_Y;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN] = PILLAR_Y;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP] = PILLAR_Y;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.DOWN] = PILLAR_Y;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN] = PILLAR_Z;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN] = PILLAR_X;
        openSidesMap[DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.WEST | DirectionBitMasks.UP | DirectionBitMasks.DOWN] = PILLAR_Z;
        openSidesMap[DirectionBitMasks.NORTH | DirectionBitMasks.SOUTH | DirectionBitMasks.EAST | DirectionBitMasks.UP | DirectionBitMasks.DOWN] = PILLAR_X;
    }

    private final VoxelShape occlusionShape;

    ChoppedLogShape(String name, int openSides) {
        this.name = name;
        this.openSides = (byte) openSides;

        this.radiusBoxes = IntStream.range(1, MAX_RADIUS)
                .boxed()
                .collect(Collectors.toMap(
                        radius -> radius,
                        this::bakeBoundingBox
                ));

        // NOTE: this must be kept independent of dynamics (e.g. level, pos) since it is used to bake models
        this.occlusionShape = Shapes.or(
                Shapes.empty(),
                Arrays.stream(Direction.values())
                        .filter(direction -> direction.getAxis().isHorizontal() && !isSideOpen(direction))
                        .map(direction -> Shapes.create(FaceShape.get(direction).getBox().asAxisAlignedBB()))
                        .toArray(VoxelShape[]::new)
        );
        int x = 0;
    }

    public static ChoppedLogShape forOpenSides(byte openSides) {
        return openSidesMap[openSides];
    }

    public Set<Direction> getSolidSides(BlockGetter level, BlockPos pos) {
        return ConfigHandler.removeBarkOnInteriorLogs.get()
                ? Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis().isHorizontal() && !isSideOpen(direction))
                .filter(direction -> {
                    BlockPos neighborPos = pos.relative(direction);
                    BlockState blockState = level.getBlockState(neighborPos);
                    return blockState.isCollisionShapeFullBlock(level, neighborPos);
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Direction.class)))
                : Collections.emptySet();
    }

    public String toString() {
        return this.name;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getSerializedName() {
        return this.name;
    }

    private AABB bakeBoundingBox(int radius) {
        boolean down = isSideOpen(Direction.DOWN);
        boolean up = isSideOpen(Direction.UP);
        boolean north = isSideOpen(Direction.NORTH);
        boolean south = isSideOpen(Direction.SOUTH);
        boolean west = isSideOpen(Direction.WEST);
        boolean east = isSideOpen(Direction.EAST);

        int chops = MAX_RADIUS - radius;
        float xCenter = 8 + (west ? chops : 0) - (east ? chops : 0);
        float yCenter = 8 + (down ? chops : 0) - (up ? chops : 0);
        float zCenter = 8 + (north ? chops : 0) - (south ? chops : 0);
        float xRadius = (west || east) ? radius : MAX_RADIUS;
        float yRadius = (down || up) ? radius : MAX_RADIUS;
        float zRadius = (north || south) ? radius : MAX_RADIUS;

        return new AABB(
                xCenter - xRadius,
                yCenter - yRadius,
                zCenter - zRadius,
                xCenter + xRadius,
                yCenter + yRadius,
                zCenter + zRadius
        );
    }

    public AABB getBoundingBox(int radius) {
        return radiusBoxes.get(Math.max(MIN_RADIUS, Math.min(radius, MAX_RADIUS)));
    }

    public boolean isSideOpen(Direction side) {
        return ((openSides >> side.ordinal()) & 0b1) == 1;
    }

    public VoxelShape getOcclusionShape() {
        return (ConfigHandler.removeBarkOnInteriorLogs.get())
                ? occlusionShape
                : Shapes.empty();
    }
}
