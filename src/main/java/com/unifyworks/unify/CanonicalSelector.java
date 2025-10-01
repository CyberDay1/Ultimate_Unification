package com.unifyworks.unify;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Optional;

/** Picks a canonical item from a tag, preferring UnifyWorks namespace. */
public final class CanonicalSelector {
    private CanonicalSelector() {}

    public static Optional<Item> pick(TagKey<Item> tag) {
        var registry = BuiltInRegistries.ITEM;
        var holders = registry.getTag(tag).orElse(null);
        if (holders == null) return Optional.empty();

        Item fallback = null;
        for (Holder<Item> h : holders) {
            Item it = h.value();
            ResourceLocation id = registry.getKey(it);
            if (id == null) continue;
            if (id.getNamespace().equals("unifyworks")) return Optional.of(it);
            if (fallback == null) fallback = it;
        }
        return Optional.ofNullable(fallback);
    }
}
