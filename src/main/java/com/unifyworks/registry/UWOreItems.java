package com.unifyworks.registry;

import com.unifyworks.UnifyWorks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

public final class UWOreItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(NeoForgeRegistries.ITEMS, UnifyWorks.MODID);

    public static final Map<String, RegistryObject<Item>> STONE = new HashMap<>();
    public static final Map<String, RegistryObject<Item>> DEEPSLATE = new HashMap<>();
    public static final Map<String, RegistryObject<Item>> NETHERRACK = new HashMap<>();

    public static void bootstrap() {
        UWOres.STONE.forEach((k, v) -> STONE.put(k, ITEMS.register(k + "_ore", () -> new BlockItem(v.get(), new Item.Properties()))));
        UWOres.DEEPSLATE.forEach((k, v) -> DEEPSLATE.put(k, ITEMS.register("deepslate_" + k + "_ore", () -> new BlockItem(v.get(), new Item.Properties()))));
        if (UWOres.NETHERRACK.containsKey("quartz")) {
            NETHERRACK.put("quartz", ITEMS.register("netherrack_quartz_ore", () -> new BlockItem(UWOres.NETHERRACK.get("quartz").get(), new Item.Properties())));
        }
    }

    private UWOreItems() {}
}
