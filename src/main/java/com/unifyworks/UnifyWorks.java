package com.unifyworks;

import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(UnifyWorks.MODID)
public class UnifyWorks {
    public static final String MODID = "unifyworks";

    public UnifyWorks() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        var snap = MaterialsIndex.loadBootstrap();
        UWItems.ITEMS.register(modBus);
        UWBlocks.BLOCKS.register(modBus);
        UWOres.BLOCKS.register(modBus);
        UWOreItems.ITEMS.register(modBus);
        UWCompressed.ITEMS.register(modBus);
        UWCompressed.BLOCKS.register(modBus);
        UWCreativeTab.TABS.register(modBus);

        UWItems.bootstrap(snap.metals, snap.gems);
        UWBlocks.bootstrap(snap.metals, snap.gems);
        UWOres.bootstrap(snap);
        UWOreItems.bootstrap();
        UWCompressed.bootstrap(snap, 9);

        modBus.addListener(this::onCommonSetup);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {}
}
