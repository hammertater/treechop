package ht.treechop.common.loot;

import ht.treechop.TreeChop;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TreeChopLootContextParams {
    public static final LootContextParam<Integer> BLOCK_CHOP_COUNT = new LootContextParam<>(TreeChop.resource("block_chop_count"));
}
