package com.unifyworks;

import com.unifyworks.config.UWConfig;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.loot.LootHooks;
import com.unifyworks.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UnifyWorks.MODID)
public class UnifyWorks {
    public static final String MODID = "unifyworks";

    public UnifyWorks() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, UWConfig.COMMON_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        MaterialsIndex.Snapshot snap = MaterialsIndex.loadBootstrap()
                .filtered(UWConfig.denyMaterials(), UWConfig.denyStones());
        UWItems.ITEMS.register(modBus);
        UWBlocks.BLOCKS.register(modBus);
        UWOres.BLOCKS.register(modBus);
        UWOreItems.ITEMS.register(modBus);
        if (UWConfig.compressionEnabled()) {
            UWCompressed.ITEMS.register(modBus);
            UWCompressed.BLOCKS.register(modBus);
        }
        UWCreativeTab.TABS.register(modBus);
        LootHooks.init(modBus);

        UWItems.bootstrap(snap.metals, snap.gems);
        UWBlocks.bootstrap(snap.metals, snap.gems);
        UWOres.bootstrap(snap);
        UWOreItems.bootstrap();
        if (UWConfig.compressionEnabled()) {
            MaterialsIndex.Snapshot compressionSnapshot = new MaterialsIndex.Snapshot();
            if (UWConfig.compressionMetals()) {
                compressionSnapshot.metals.addAll(snap.metals);
            }
            if (UWConfig.compressionGems()) {
                compressionSnapshot.gems.addAll(snap.gems);
            }
            if (UWConfig.compressionStones()) {
                compressionSnapshot.stones.addAll(snap.stones);
            }

            UWCompressed.bootstrap(compressionSnapshot, UWConfig.maxTier());
        }

        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {}
}
