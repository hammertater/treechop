package ht.treechop.common.loot;

import ht.treechop.TreeChop;
import ht.treechop.mixin.LootContextParamSetsAccess;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Set;
import java.util.stream.Collectors;

public class TreeChopLootContextParams {
    public static void init() {

    }

    public static final LootContextParam<Integer> BLOCK_CHOP_COUNT = new LootContextParam<>(TreeChop.resource("block_chop_count"));
    public static final LootContextParam<Boolean> DESTROY_BLOCK = new LootContextParam<>(TreeChop.resource("destroy_block"));

    public static final LootContextParamSet SET = LootContextParamSetsAccess.callRegister("treechop:chopping", set -> {
        Set<LootContextParam<?>> required = LootContextParamSets.BLOCK.getRequired();
        Set<LootContextParam<?>> optional = LootContextParamSets.BLOCK.getAllowed().stream().filter(p -> !required.contains(p)).collect(Collectors.toSet());

        required.forEach(set::required);
        optional.forEach(set::optional);

        set.required(BLOCK_CHOP_COUNT).required(DESTROY_BLOCK);

        set.build();
    });
}
