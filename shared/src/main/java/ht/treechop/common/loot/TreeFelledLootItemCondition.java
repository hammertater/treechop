package ht.treechop.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import ht.treechop.TreeChop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.Collections;
import java.util.Set;

public class TreeFelledLootItemCondition implements LootItemCondition {
    public static final ResourceLocation ID = TreeChop.resource("tree_felled");
    public static final LootItemConditionType TYPE = new LootItemConditionType(new Serializer());
    static final TreeFelledLootItemCondition INSTANCE = new TreeFelledLootItemCondition();

    TreeFelledLootItemCondition() {
    }

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

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TreeFelledLootItemCondition> {
        public void serialize(JsonObject json, TreeFelledLootItemCondition condition, JsonSerializationContext context) {
        }

        public TreeFelledLootItemCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return INSTANCE;
        }
    }
}