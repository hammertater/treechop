package ht.treechop.common.properties;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.FaceShape;
import javafx.geometry.BoundingBox;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ht.treechop.common.util.DirectionBitMasks.DOWN;
import static ht.treechop.common.util.DirectionBitMasks.EAST;
import static ht.treechop.common.util.DirectionBitMasks.NORTH;
import static ht.treechop.common.util.DirectionBitMasks.SOUTH;
import static ht.treechop.common.util.DirectionBitMasks.UP;
import static ht.treechop.common.util.DirectionBitMasks.WEST;

public enum ChoppedLogShape implements IStringSerializable {
    PILLAR_Y("pillar", NORTH | SOUTH | EAST | WEST), // "pillar" instead of "pillar_y" for backwards compatibility
    CORNER_NWEU("corner_nweu", NORTH | EAST | WEST | UP),
    CORNER_NWED("corner_nwed", NORTH | EAST | WEST | DOWN),
    CORNER_NSWU("corner_nswu", NORTH | SOUTH | WEST | UP),
    CORNER_NSWD("corner_nswd", NORTH | SOUTH | WEST | DOWN),
    CORNER_NWUD("corner_nwud", NORTH | WEST | UP | DOWN),
    CORNER_SWEU("corner_sweu", SOUTH | EAST | WEST | UP),
    CORNER_SWED("corner_swed", SOUTH | EAST | WEST | DOWN),
    PILLAR_Z("pillar_z", EAST | WEST | UP | DOWN),
    CORNER_SWUD("corner_swud", SOUTH | WEST | UP | DOWN),
    CORNER_NSEU("corner_nseu", NORTH | SOUTH | EAST | UP),
    CORNER_NSED("corner_nsed", NORTH | SOUTH | EAST | DOWN),
    CORNER_NEUD("corner_neud", NORTH | EAST | UP | DOWN),
    PILLAR_X("pillar_x", NORTH | SOUTH | UP | DOWN),
    CORNER_SEUD("corner_seud", SOUTH | EAST | UP | DOWN),
    END_N("end_n", NORTH | EAST | WEST),
    END_W("end_w", NORTH | SOUTH | WEST),
    CORNER_NWU("corner_nwu", NORTH | WEST | UP),
    CORNER_NWD("corner_nwd", NORTH | WEST | DOWN),
    END_S("end_s", SOUTH | EAST | WEST),
    CORNER_WEU("corner_weu", EAST | WEST | UP),
    CORNER_WED("corner_wed", EAST | WEST | DOWN),
    CORNER_SWU("corner_swu", SOUTH | WEST | UP),
    CORNER_SWD("corner_swd", SOUTH | WEST | DOWN),
    CORNER_WUD("corner_wud", WEST | UP | DOWN),
    END_E("end_e", NORTH | SOUTH | EAST),
    CORNER_NEU("corner_neu", NORTH | EAST | UP),
    CORNER_NED("corner_ned", NORTH | EAST | DOWN),
    END_U("end_u", NORTH | SOUTH | UP),
    END_D("end_d", NORTH | SOUTH | DOWN),
    CORNER_NUD("corner_nud", NORTH | UP | DOWN),
    CORNER_SEU("corner_seu", SOUTH | EAST | UP),
    CORNER_SED("corner_sed", SOUTH | EAST | DOWN),
    CORNER_EUD("corner_eud", EAST | UP | DOWN),
    CORNER_SUD("corner_sud", SOUTH | UP | DOWN),
    CORNER_NW("corner_nw", NORTH | WEST),
    SLAB_YZ("slab_yz", EAST | WEST),
    CORNER_SW("corner_sw", SOUTH | WEST),
    CORNER_WU("corner_wu", WEST | UP),
    CORNER_WD("corner_wd", WEST | DOWN),
    CORNER_NE("corner_ne", NORTH | EAST),
    SLAB_XY("slab_xy", NORTH | SOUTH),
    CORNER_NU("corner_nu", NORTH | UP),
    CORNER_ND("corner_nd", NORTH | DOWN),
    CORNER_SE("corner_se", SOUTH | EAST),
    CORNER_EU("corner_eu", EAST | UP),
    CORNER_ED("corner_ed", EAST | DOWN),
    CORNER_SU("corner_su", SOUTH | UP),
    CORNER_SD("corner_sd", SOUTH | DOWN),
    SLAB_XZ("slab_xz", UP | DOWN),
    SIDE_W("side_w", WEST),
    SIDE_N("side_n", NORTH),
    SIDE_E("side_e", EAST),
    SIDE_S("side_s", SOUTH),
    SIDE_U("side_u", UP),
    SIDE_D("side_d", DOWN);

    private final String name;
    private final byte openSides;
    private final Map<Integer, BoundingBox> chopsBoxes;

    private static final ChoppedLogShape[] openSidesMap
            = new ChoppedLogShape[(NORTH | SOUTH | EAST | WEST | UP | DOWN) + 1];

    static {
        Arrays.stream(ChoppedLogShape.values()).forEach(shape -> openSidesMap[shape.openSides] = shape);
        openSidesMap[0] = PILLAR_Y;
        openSidesMap[NORTH | SOUTH | EAST | WEST | UP | DOWN] = PILLAR_Y;
        openSidesMap[NORTH | SOUTH | EAST | WEST | UP] = PILLAR_Y;
        openSidesMap[NORTH | SOUTH | EAST | WEST | DOWN] = PILLAR_Y;
        openSidesMap[NORTH | EAST | WEST | UP | DOWN] = PILLAR_Z;
        openSidesMap[NORTH | SOUTH | WEST | UP | DOWN] = PILLAR_X;
        openSidesMap[SOUTH | EAST | WEST | UP | DOWN] = PILLAR_Z;
        openSidesMap[NORTH | SOUTH | EAST | UP | DOWN] = PILLAR_X;
    }

    private final VoxelShape occlusionShape;

    ChoppedLogShape(String name, int openSides) {
        this.name = name;
        this.openSides = (byte) openSides;

        this.chopsBoxes = IntStream.rangeClosed(1, 7)
                .boxed()
                .collect(Collectors.toMap(
                        chops -> chops,
                        this::bakeBoundingBox
                ));

        // NOTE: this must be kept independent of dynamics (e.g. world, pos) since it is used to bake models
        this.occlusionShape = VoxelShapes.or(
                VoxelShapes.empty(),
                Arrays.stream(Direction.values())
                        .filter(direction -> direction.getAxis().isHorizontal())
                        .filter(direction -> !isSideOpen(direction))
                        .map(direction -> VoxelShapes.create(FaceShape.get(direction).getBox()))
                        .toArray(VoxelShape[]::new)
        );
    }

    public static ChoppedLogShape forOpenSides(byte openSides) {
        return openSidesMap[openSides];
    }

    public String toString() {
        return this.name;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String getString() {
        return this.name;
    }

    private BoundingBox bakeBoundingBox(int chops) {
        boolean down = isSideOpen(Direction.DOWN);
        boolean up = isSideOpen(Direction.UP);
        boolean north = isSideOpen(Direction.NORTH);
        boolean south = isSideOpen(Direction.SOUTH);
        boolean west = isSideOpen(Direction.WEST);
        boolean east = isSideOpen(Direction.EAST);

        float xCenter = 8 + (west ? chops : 0) - (east ? chops : 0);
        float yCenter = 8 + (down ? chops : 0) - (up ? chops : 0);
        float zCenter = 8 + (north ? chops : 0) - (south ? chops : 0);
        float xRadius = (west || east) ? 8 - chops : 8;
        float yRadius = (down || up) ? 8 - chops : 8;
        float zRadius = (north || south) ? 8 - chops : 8;

        return new BoundingBox(
                xCenter - xRadius,
                yCenter - yRadius,
                zCenter - zRadius,
                xRadius * 2,
                yRadius * 2,
                zRadius * 2
        );
    }

    public BoundingBox getBoundingBox(int chops) {
        return chopsBoxes.get(chops);
    }

    public boolean isSideOpen(Direction side) {
        return ((openSides >> side.ordinal()) & 0b1) == 1;
    }

    public VoxelShape getOcclusionShape() {
        return (ConfigHandler.CLIENT.removeBarkOnInteriorLogs.get())
                ? occlusionShape
                : VoxelShapes.empty();
    }
}
