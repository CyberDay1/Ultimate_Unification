package com.unifyworks.unify;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CanonicalFamilies {
    public record Family(String key, String tagPath, String prefix, String suffix, List<String> namespaces) {
        public Family(String key, String tagPath, String prefix, String suffix, String... namespaces) {
            this(key, tagPath, prefix, suffix, List.of(namespaces));
        }

        public String extractMaterial(ResourceLocation itemId) {
            if (itemId == null) return null;
            String path = itemId.getPath();
            if (prefix != null && !prefix.isEmpty()) {
                if (!path.startsWith(prefix)) return null;
                path = path.substring(prefix.length());
            }
            if (suffix != null && !suffix.isEmpty()) {
                if (!path.endsWith(suffix)) return null;
                path = path.substring(0, path.length() - suffix.length());
            }
            return path.isEmpty() ? null : path;
        }
    }

    public record TagCheck(ResourceLocation tagId, boolean present, int entryCount, boolean containsHeld,
                           Item canonicalItem, ResourceLocation canonicalId) {
        public boolean hasCanonical() {
            return canonicalItem != null;
        }

        public boolean canonicalMatches(Item item) {
            return canonicalItem != null && canonicalItem == item;
        }
    }

    public record FamilyDiagnostics(Family family, String material, ResourceLocation itemId, List<TagCheck> tags) {
        public Optional<TagCheck> firstPresentTag() {
            return tags.stream().filter(TagCheck::present).findFirst();
        }

        public Optional<TagCheck> tagContainingHeld() {
            return tags.stream().filter(TagCheck::containsHeld).findFirst();
        }
    }

    private static final List<Family> FAMILIES = List.of(
            new Family("ingot", "ingots", null, "_ingot", "forge", "c"),
            new Family("gem", "gems", null, "_gem", "forge", "c"),
            new Family("raw", "raw_materials", "raw_", null, "forge", "c"),
            new Family("dust", "dusts", null, "_dust", "forge", "c"),
            new Family("nugget", "nuggets", null, "_nugget", "forge", "c"),
            new Family("deepslate_ore", "ores", "deepslate_", "_ore", "forge", "c"),
            new Family("nether_ore", "ores", "netherrack_", "_ore", "forge", "c"),
            new Family("ore", "ores", null, "_ore", "forge", "c")
    );

    private CanonicalFamilies() {}

    public static List<Family> all() {
        return FAMILIES;
    }

    public static Optional<FamilyDiagnostics> diagnose(Item item) {
        if (item == null) return Optional.empty();
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        if (itemId == null) return Optional.empty();

        for (Family family : FAMILIES) {
            String material = family.extractMaterial(itemId);
            if (material == null || material.isEmpty()) continue;

            List<TagCheck> tags = new ArrayList<>();
            for (String namespace : family.namespaces()) {
                ResourceLocation tagId = new ResourceLocation(namespace.toLowerCase(Locale.ROOT),
                        family.tagPath() + "/" + material);
                TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
                var holdersOpt = BuiltInRegistries.ITEM.getTag(tag);
                boolean present = holdersOpt.isPresent();
                boolean containsHeld = false;
                int entryCount = 0;
                if (present) {
                    for (Holder<Item> holder : holdersOpt.get()) {
                        entryCount++;
                        if (holder.value() == item) {
                            containsHeld = true;
                        }
                    }
                }
                Item canonicalItem = CanonicalSelector.pick(tag).orElse(null);
                ResourceLocation canonicalId = canonicalItem == null ? null : BuiltInRegistries.ITEM.getKey(canonicalItem);
                tags.add(new TagCheck(tagId, present, entryCount, containsHeld, canonicalItem, canonicalId));
            }
            return Optional.of(new FamilyDiagnostics(family, material, itemId, List.copyOf(tags)));
        }
        return Optional.empty();
    }
}
