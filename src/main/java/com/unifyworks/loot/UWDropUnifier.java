package com.unifyworks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Redirect ore drops to canonical raw_* or *_gem. Tag-driven, minimal logic.
 * Fortune is handled by vanilla when replacing stack counts; we keep counts as-is.
 */
public class UWDropUnifier extends LootModifier {
    public static final Codec<UWDropUnifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC)
                    .fieldOf("ore_tag_outputs")
                    .forGetter(m -> m.oreTagOutputs)
    ).apply(inst, UWDropUnifier::new));

    private final Map<ResourceLocation, ResourceLocation> oreTagOutputs;
    private final Map<TagKey<Block>, ResourceLocation> resolvedOutputs;

    public UWDropUnifier(LootItemCondition[] conditions, Map<ResourceLocation, ResourceLocation> oreTagOutputs) {
        super(conditions);
        this.oreTagOutputs = oreTagOutputs;
        this.resolvedOutputs = new HashMap<>();
        for (var entry : oreTagOutputs.entrySet()) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, entry.getKey());
            this.resolvedOutputs.put(tag, entry.getValue());
        }
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        BlockState state = ctx.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return generatedLoot;

        ResourceLocation outputId = null;
        for (var entry : resolvedOutputs.entrySet()) {
            if (state.is(entry.getKey())) {
                outputId = entry.getValue();
                break;
            }
        }
        if (outputId == null) {
            return generatedLoot;
        }

        Item item = BuiltInRegistries.ITEM.getOptional(outputId).orElse(null);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return generatedLoot;
        }

        int total = 0;
        for (var stack : generatedLoot) {
            total += stack.getCount();
        }
        if (total <= 0) {
            return generatedLoot;
        }

        generatedLoot.clear();
        generatedLoot.add(new ItemStack(item, Math.max(1, total)));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
