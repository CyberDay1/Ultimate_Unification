package com.unifyworks;

import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import com.unifyworks.registry.UWItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(UnifyWorks.MODID)
public class UnifyWorks {
    public static final String MODID = "unifyworks";

    public UnifyWorks(IEventBus modBus) {
        // bootstrap registries from data snapshot before freeze
        var snap = MaterialsIndex.loadBootstrap();
        UWItems.bootstrap(snap.metals, snap.gems);
        UWBlocks.bootstrap(snap.metals, snap.gems);
        UWItems.ITEMS.register(modBus);
        UWBlocks.BLOCKS.register(modBus);

        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        // future: recipe unifier, loot hooks, compression, worldgen wiring
    }
}
