package ht.treechop.state.properties;

import net.minecraft.util.IStringSerializable;

public enum ChoppedLogShape implements IStringSerializable {
    PILLAR("pillar"),
    CORNER_NW("corner_nw"),
    CORNER_NE("corner_ne"),
    CORNER_SE("corner_se"),
    CORNER_SW("corner_sw"),
    END_W("end_w"),
    END_N("end_n"),
    END_E("end_e"),
    END_S("end_s"),
    SIDE_W("side_w"),
    SIDE_N("side_n"),
    SIDE_E("side_e"),
    SIDE_S("side_s");

    private final String name;

    ChoppedLogShape(String name) { this.name = name; }

    public String toString() {
        return this.name;
    }

    @Override
    public String func_176610_l() { return this.name; }
}
