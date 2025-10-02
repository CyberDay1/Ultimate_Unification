package com.unifyworks.data;

import com.unifyworks.UnifyWorks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod.EventBusSubscriber(modid = UnifyWorks.MODID)
public final class DataHooks {
    private DataHooks() {}

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new UnifyDataReload());
    }
}
