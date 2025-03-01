package ht.treechop.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ht.treechop.TreeChop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public record CountBlockChopsLootItemCondition(IntRange range) implements LootItemCondition {
    public static final ResourceLocation ID = TreeChop.resource("count_block_chops");
    public static final MapCodec<CountBlockChopsLootItemCondition> CODEC = RecordCodecBuilder.mapCodec(
            p_297208_ -> p_297208_.group(
                            IntRange.CODEC.fieldOf("range").forGetter(CountBlockChopsLootItemCondition::range)
                    )
                    .apply(p_297208_, CountBlockChopsLootItemCondition::new)
    );
    public static final LootItemConditionType TYPE = new LootItemConditionType(CODEC);

    public LootItemConditionType getType() {
        return TYPE;
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return range.getReferencedContextParams();
    }

    public boolean test(LootContext context) {
        Integer count = context.getParamOrNull(TreeChopLootContextParams.BLOCK_CHOP_COUNT);
        return count != null && this.range.test(context, count);
    }
}