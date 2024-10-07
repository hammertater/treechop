package ht.treechop.common.loot;

import com.mojang.serialization.MapCodec;
import ht.treechop.TreeChop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Collections;
import java.util.Set;

public record TreeFelledLootItemCondition() implements LootItemCondition {
    public static final ResourceLocation ID = TreeChop.resource("tree_felled");
    static final TreeFelledLootItemCondition INSTANCE = new TreeFelledLootItemCondition();
    public static final MapCodec<TreeFelledLootItemCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    public LootItemConditionType getType() {
        return TYPE;
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Collections.emptySet();
    }

    public boolean test(LootContext context) {
        Boolean destroying = context.getParamOrNull(TreeChopLootContextParams.DESTROY_BLOCK);
        return destroying == null || destroying;
    }
}