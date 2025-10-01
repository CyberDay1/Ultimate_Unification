package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Canonical items: raw chunks, nuggets, and base items (ingot/gem). */
public class UWItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UnifyWorks.MODID);

    public static final Map<String, DeferredItem<Item>> RAW_MATERIALS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> NUGGETS = new LinkedHashMap<>();
    public static final Map<String, DeferredItem<Item>> BASE_ITEMS = new LinkedHashMap<>(); // ingot or gem

    public static void bootstrap(List<String> metals, List<String> gems) {
        RAW_MATERIALS.clear();
        NUGGETS.clear();
        BASE_ITEMS.clear();
        for (String metal : metals) {
            registerMetalItems(metal);
        }
        for (String gem : gems) {
            registerGemItems(gem);
        }
    }

    private static void registerMetalItems(String name) {
        RAW_MATERIALS.put(name, ITEMS.register("raw_" + name, () -> new Item(new Item.Properties())));
        NUGGETS.put(name, ITEMS.register("nugget_" + name, () -> new Item(new Item.Properties())));
        BASE_ITEMS.put(name, ITEMS.register(name + "_ingot", () -> new Item(new Item.Properties())));
    }

    private static void registerGemItems(String name) {
        NUGGETS.put(name, ITEMS.register("nugget_" + name, () -> new Item(new Item.Properties())));
        BASE_ITEMS.put(name, ITEMS.register(name + "_gem", () -> new Item(new Item.Properties())));
    }
}
