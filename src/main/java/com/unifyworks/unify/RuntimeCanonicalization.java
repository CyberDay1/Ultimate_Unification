package com.unifyworks.unify;

import com.unifyworks.UnifyWorks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = UnifyWorks.MODID)
public final class RuntimeCanonicalization {
    private record FamilySpec(String tagPath, String prefix, String suffix, List<String> namespaces) {
        FamilySpec(String tagPath, String prefix, String suffix, String... namespaces) {
            this(tagPath, prefix, suffix, List.of(namespaces));
        }

        String extractMaterial(String itemPath) {
            if (suffix != null && itemPath.endsWith(suffix)) {
                return itemPath.substring(0, itemPath.length() - suffix.length());
            }
            if (prefix != null && itemPath.startsWith(prefix)) {
                return itemPath.substring(prefix.length());
            }
            return null;
        }
    }

    private static final List<FamilySpec> FAMILIES = List.of(
            new FamilySpec("ingots", null, "_ingot", "forge", "c"),
            new FamilySpec("gems", null, "_gem", "forge", "c"),
            new FamilySpec("raw_materials", "raw_", null, "forge", "c"),
            new FamilySpec("dusts", null, "_dust", "forge", "c"),
            new FamilySpec("nuggets", null, "_nugget", "forge", "c")
    );

    private RuntimeCanonicalization() {}

    @SubscribeEvent
    public static void onCrafted(PlayerEvent.ItemCraftedEvent evt) {
        canonicalize(evt.getCrafting());
    }

    @SubscribeEvent
    public static void onSmelted(PlayerEvent.ItemSmeltedEvent evt) {
        canonicalize(evt.getSmelting());
    }

    private static void canonicalize(ItemStack stack) {
        if (stack.isEmpty()) return;
        var registry = BuiltInRegistries.ITEM;
        var id = registry.getKey(stack.getItem());
        if (id == null) return;
        String itemPath = id.getPath();

        for (FamilySpec spec : FAMILIES) {
            String material = spec.extractMaterial(itemPath);
            if (material == null || material.isEmpty()) continue;

            for (String namespace : spec.namespaces()) {
                ResourceLocation tagId = new ResourceLocation(namespace, spec.tagPath() + "/" + material);
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                var pick = CanonicalSelector.pick(tag);
                if (pick.isPresent() && stack.getItem() != pick.get()) {
                    int count = stack.getCount();
                    stack.setItem(pick.get());
                    stack.setCount(count);
                    return;
                }
            }
        }
    }
}
