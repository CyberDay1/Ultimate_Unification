package com.unifyworks.loot;

import net.neoforged.bus.api.IEventBus;

public final class LootHooks {
    private LootHooks() {}

    public static void init(IEventBus modBus) {
        UWLootSerializers.LOOT_MODIFIERS.register(modBus);
    }
}
