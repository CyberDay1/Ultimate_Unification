package com.unifyworks.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.unifyworks.config.UWWorldgenConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public record ConditionalRemoveFeatures(HolderSet<Biome> biomes,
                                        HolderSet<PlacedFeature> features,
                                        GenerationStep.Decoration step,
                                        ConditionalAddFeatures.Target target) implements BiomeModifier {
    public static final Codec<ConditionalRemoveFeatures> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConditionalRemoveFeatures::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConditionalRemoveFeatures::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConditionalRemoveFeatures::step),
            ConditionalAddFeatures.Target.CODEC.optionalFieldOf("target", ConditionalAddFeatures.Target.ANY)
                    .forGetter(ConditionalRemoveFeatures::target)
    ).apply(inst, ConditionalRemoveFeatures::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.REMOVE) return;
        if (!biomes.contains(biome)) return;
        if (!UWWorldgenConfig.enabled()) return;
        if (!UWWorldgenConfig.prune()) return;
        boolean allow = switch (target) {
            case OVERWORLD -> UWWorldgenConfig.overworld();
            case NETHER -> UWWorldgenConfig.nether();
            case END -> UWWorldgenConfig.end();
            case ANY -> true;
        };
        if (!allow) return;
        builder.getGenerationSettings().getFeatures(step).removeIf(features::contains);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
