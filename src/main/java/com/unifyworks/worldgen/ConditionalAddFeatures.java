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

public record ConditionalAddFeatures(HolderSet<Biome> biomes,
                                     HolderSet<PlacedFeature> features,
                                     GenerationStep.Decoration step,
                                     Target target) implements BiomeModifier {
    public static final Codec<ConditionalAddFeatures> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(ConditionalAddFeatures::biomes),
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(ConditionalAddFeatures::features),
            GenerationStep.Decoration.CODEC.fieldOf("step").forGetter(ConditionalAddFeatures::step),
            Target.CODEC.fieldOf("target").forGetter(ConditionalAddFeatures::target)
    ).apply(inst, ConditionalAddFeatures::new));

    public enum Target {
        OVERWORLD,
        NETHER,
        END,
        ANY;

        public static final Codec<Target> CODEC = Codec.STRING.xmap(
                s -> Target.valueOf(s.toUpperCase()),
                t -> t.name().toLowerCase()
        );
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) return;
        if (!biomes.contains(biome)) return;
        if (!UWWorldgenConfig.enabled()) return;
        boolean allow = switch (target) {
            case OVERWORLD -> UWWorldgenConfig.overworld();
            case NETHER -> UWWorldgenConfig.nether();
            case END -> UWWorldgenConfig.end();
            case ANY -> true;
        };
        if (!allow) return;
        builder.getGenerationSettings().addFeature(step, features);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
