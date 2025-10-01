package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Canonical items: nuggets and base items (ingot/gem). */
public class UWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UnifyWorks.MODID);

    public static final Map<String, DeferredItem<Item>> NUGGETS = new HashMap<>();
    public static final Map<String, DeferredItem<Item>> BASE_ITEMS = new HashMap<>(); // ingot or gem

    public static void bootstrap(List<String> metals, List<String> gems) {
        NUGGETS.clear();
        BASE_ITEMS.clear();
        for (String m : metals) {
            NUGGETS.put(m, ITEMS.register("nugget_" + m, () -> new Item(new Item.Properties())));
            BASE_ITEMS.put(m, ITEMS.register(m + "_ingot", () -> new Item(new Item.Properties())));
        }
        for (String g : gems) {
            NUGGETS.put(g, ITEMS.register("nugget_" + g, () -> new Item(new Item.Properties())));
            BASE_ITEMS.put(g, ITEMS.register(g + "_gem", () -> new Item(new Item.Properties())));
        }
    }
}
