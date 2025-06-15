package ht.treechop.common.loot;

import ht.treechop.TreeChop;
import ht.treechop.mixin.LootContextParamSetsAccess;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Set;
import java.util.stream.Collectors;

public class TreeChopLootContextParams {
    public static void init() {

    }

    public static final ContextKey<Integer> BLOCK_CHOP_COUNT = new ContextKey<>(TreeChop.resource("count_block_chops"));
    public static final ContextKey<Boolean> DESTROY_BLOCK = new ContextKey<>(TreeChop.resource("tree_felled"));

    public static final ContextKeySet SET = LootContextParamSetsAccess.callRegister("treechop", set -> {
        Set<ContextKey<?>> required = LootContextParamSets.BLOCK.required();
        Set<ContextKey<?>> optional = LootContextParamSets.BLOCK.allowed().stream().filter(p -> !required.contains(p)).collect(Collectors.toSet());

        required.forEach(set::required);
        optional.forEach(set::optional);

        set.required(BLOCK_CHOP_COUNT).required(DESTROY_BLOCK);

        set.build();
    });
}
