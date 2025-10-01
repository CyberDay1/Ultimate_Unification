package com.unifyworks.loot;

import com.mojang.serialization.Codec;
import com.unifyworks.UnifyWorks;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public final class UWLootSerializers {
    private UWLootSerializers() {}

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, UnifyWorks.MODID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> DROP_UNIFIER =
            LOOT_MODIFIERS.register("drop_unifier", () -> UWDropUnifier.CODEC);
}
