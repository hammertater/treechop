package ht.treechop.common.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ht.treechop.TreeChop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Set;

public class BlockChopCountLootItemCondition implements LootItemCondition {
    public static final ResourceLocation ID = TreeChop.resource("block_chop_count");
    public static final LootItemConditionType TYPE = new LootItemConditionType(new Serializer());

    final IntRange range;

    BlockChopCountLootItemCondition(IntRange range) {
        this.range = range;
    }

    public LootItemConditionType getType() {
        return TYPE;
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Sets.union(ImmutableSet.of(TreeChopLootContextParams.BLOCK_CHOP_COUNT), range.getReferencedContextParams());
    }

    public boolean test(LootContext context) {
        Integer count = context.getParamOrNull(TreeChopLootContextParams.BLOCK_CHOP_COUNT);
        return count != null && this.range.test(context, count);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<BlockChopCountLootItemCondition> {
        public void serialize(JsonObject json, BlockChopCountLootItemCondition condition, JsonSerializationContext context) {
            json.add("range", context.serialize(condition.range));
        }

        public BlockChopCountLootItemCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            IntRange range = GsonHelper.getAsObject(json, "range", context, IntRange.class);
            return new BlockChopCountLootItemCondition(range);
        }
    }
}