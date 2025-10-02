package com.unifyworks.api;

import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.unify.CanonicalFamilies;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/**
 * Lightweight helpers that expose UnifyWorks' canonical material resolution for other mods and scripts.
 */
public final class CanonicalAPI {
    private CanonicalAPI() {
    }

    private static MaterialsIndex.Snapshot bootstrap() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static final MaterialsIndex.Snapshot INSTANCE = MaterialsIndex.loadBootstrap();

        private Holder() {
        }
    }

    /**
     * Represents a canonical material match for a given item or stack.
     */
    public record CanonicalMatch(CanonicalFamilies.Family family,
                                 String materialId,
                                 String canonicalMaterialId,
                                 Item canonicalItem,
                                 ResourceLocation canonicalItemId) {
        public boolean hasCanonicalItem() {
            return canonicalItem != null && canonicalItemId != null;
        }

        public ItemStack createStack(int count) {
            if (!hasCanonicalItem()) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(canonicalItem, count);
        }
    }

    /**
     * Resolves canonical information for the given stack.
     */
    public static Optional<CanonicalMatch> resolve(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return resolve(stack.getItem());
    }

    /**
     * Resolves canonical information for the given item.
     */
    public static Optional<CanonicalMatch> resolve(Item item) {
        if (item == null) {
            return Optional.empty();
        }
        var diagnostics = CanonicalFamilies.diagnose(item);
        if (diagnostics.isEmpty()) {
            return Optional.empty();
        }

        MaterialsIndex.Snapshot snapshot = bootstrap();
        var diag = diagnostics.get();
        String material = diag.material();
        String canonicalMaterial = snapshot.canonicalName(material);
        if (canonicalMaterial == null) {
            canonicalMaterial = material;
        }

        for (var tag : diag.tags()) {
            if (!tag.present() || !tag.hasCanonical()) {
                continue;
            }
            Item canonical = tag.canonicalItem();
            ResourceLocation canonicalId = tag.canonicalId();
            if (canonical == null || canonicalId == null) {
                continue;
            }
            return Optional.of(new CanonicalMatch(diag.family(), material, canonicalMaterial, canonical, canonicalId));
        }

        return Optional.of(new CanonicalMatch(diag.family(), material, canonicalMaterial, null, null));
    }

    /**
     * Returns the canonical material id for the given stack if it belongs to a known family.
     */
    public static Optional<String> canonicalMaterial(ItemStack stack) {
        return resolve(stack).map(CanonicalMatch::canonicalMaterialId);
    }

    /**
     * Returns the canonical item id for the given stack if one exists.
     */
    public static Optional<ResourceLocation> canonicalItemId(ItemStack stack) {
        return resolve(stack).map(CanonicalMatch::canonicalItemId);
    }

    /**
     * Returns a canonical replacement for the provided stack. If no canonical item exists the original stack is returned.
     * The returned stack is always a new copy and the original stack is never modified.
     */
    public static ItemStack canonicalize(ItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            return stack.copy();
        }
        var match = resolve(stack);
        if (match.isEmpty()) {
            return stack.copy();
        }
        CanonicalMatch canonical = match.get();
        if (!canonical.hasCanonicalItem()) {
            return stack.copy();
        }
        Item item = stack.getItem();
        if (item == canonical.canonicalItem()) {
            return stack.copy();
        }
        ItemStack replacement = canonical.createStack(stack.getCount());
        return replacement.isEmpty() ? stack.copy() : replacement;
    }

    /**
     * Tests whether the provided stack already matches the canonical item for its family.
     */
    public static boolean isCanonical(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        var match = resolve(stack);
        if (match.isEmpty()) {
            return false;
        }
        CanonicalMatch canonical = match.get();
        if (!canonical.hasCanonicalItem()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return Objects.equals(id, canonical.canonicalItemId());
    }

    /**
     * Helper that resolves the canonical preview stack for recipe book displays.
     */
    public static ItemStack preview(ItemStack stack) {
        if (stack == null) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            return stack.copy();
        }
        ItemStack resolved = canonicalize(stack);
        if (!resolved.isEmpty()) {
            return resolved;
        }
        return stack.copy();
    }
}
