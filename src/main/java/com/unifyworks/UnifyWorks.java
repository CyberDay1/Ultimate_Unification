package com.unifyworks;

import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(UnifyWorks.MODID)
public class UnifyWorks {
    public static final String MODID = "unifyworks";

    public UnifyWorks(IEventBus modBus) {
        // TODO: Register deferred registries for items/blocks if needed
        // TODO: Hook data reload listeners for materials + compression config
        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        // TODO: Initialize services (recipe unifier, loot modifiers registration, IMC, etc.)
    }
}
