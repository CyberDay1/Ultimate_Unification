package com.unifyworks.worldgen;

import com.mojang.serialization.Codec;
import com.unifyworks.UnifyWorks;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryObject;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class UWBiomeModifiers {
    public static final DeferredRegister<Codec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, UnifyWorks.MODID);

    public static final RegistryObject<Codec<? extends BiomeModifier>> CONDITIONAL_ADD =
            SERIALIZERS.register("conditional_add_features", () -> ConditionalAddFeatures.CODEC);
    public static final RegistryObject<Codec<? extends BiomeModifier>> CONDITIONAL_REMOVE =
            SERIALIZERS.register("conditional_remove_features", () -> ConditionalRemoveFeatures.CODEC);

    private UWBiomeModifiers() {}
}
