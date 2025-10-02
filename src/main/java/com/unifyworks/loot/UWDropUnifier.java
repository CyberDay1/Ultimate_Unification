package com.unifyworks.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unifyworks.config.UWConfig;
import com.unifyworks.data.UnifyDataReload;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class UWDropUnifier extends LootModifier {
    public static final Codec<UWDropUnifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(m -> m.enabledFromData)
    ).apply(inst, UWDropUnifier::new));

    private final boolean enabledFromData;

    public UWDropUnifier(LootItemCondition[] conditions, boolean enabledFromData) {
        super(conditions);
        this.enabledFromData = enabledFromData;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        if (!enabledFromData || !UWConfig.dropUnifierEnabled()) {
            return generatedLoot;
        }

        BlockState state = ctx.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (state == null) return generatedLoot;

        Item drop = UnifyDataReload.resolveDrop(state.getBlock());
        if (drop == null) return generatedLoot;

        int total = 0;
        for (ItemStack stack : generatedLoot) {
            total += stack.getCount();
        }
        if (total <= 0) return generatedLoot;

        generatedLoot.clear();
        generatedLoot.add(new ItemStack(drop, Math.max(1, total)));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
