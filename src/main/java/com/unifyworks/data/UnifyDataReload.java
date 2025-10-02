package com.unifyworks.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.unifyworks.UnifyWorks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.TagKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class UnifyDataReload extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    private static final List<String> ORE_NAMESPACES = List.of("forge", "c");

    private static final Map<TagKey<Block>, Item> TAG_DROPS = new HashMap<>();
    private static final Map<String, Item> MATERIAL_DROPS = new HashMap<>();
    private static MaterialsIndex.Snapshot SNAPSHOT = new MaterialsIndex.Snapshot();
    private static MaterialsIndex.Merge MERGE = new MaterialsIndex.Merge();

    private static int MERGED_COMPRESSION_TIER = 9;

    public UnifyDataReload() {
        super(GSON, "unify");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager mgr, ProfilerFiller profiler) {
        TAG_DROPS.clear();
        MATERIAL_DROPS.clear();
        MaterialsIndex.Snapshot aggregated = MaterialsIndex.loadBootstrap();

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            JsonElement json = entry.getValue();
            if (json == null || !json.isJsonObject()) continue;
            if (!json.getAsJsonObject().has("materials")) continue;
            MaterialsIndex.Snapshot parsed = MaterialsIndex.fromJson(json);
            aggregated.merge(parsed);
        }

        MERGE = MaterialsIndex.loadMergeLayer(mgr);
        MaterialsIndex.Snapshot merged = aggregated.applyMerge(MERGE);
        SNAPSHOT = merged;
        MERGED_COMPRESSION_TIER = merged.maxCompressionTier();

        for (MaterialsIndex.MaterialEntry material : merged.materials()) {
            if (!material.unify()) continue;
            Item drop = canonicalDrop(material);
            if (drop == null) continue;

            MATERIAL_DROPS.put(material.name(), drop);
            for (String alias : material.aliases()) {
                MATERIAL_DROPS.put(alias, drop);
            }

            for (ResourceLocation tagId : material.oreTags()) {
                TAG_DROPS.put(TagKey.create(Registries.BLOCK, tagId), drop);
            }

            addAliasTagMappings(material.name(), drop);
            for (String alias : material.aliases()) {
                addAliasTagMappings(alias, drop);
            }
        }

        for (MaterialsIndex.AliasEntry alias : merged.oreAliases()) {
            if (alias.aliasTo() == null || alias.aliasTo().isEmpty()) continue;
            String canonical = merged.canonicalName(alias.aliasTo());
            Item drop = null;
            if (canonical != null) {
                drop = MATERIAL_DROPS.get(canonical);
            }
            if (drop == null) {
                drop = MATERIAL_DROPS.get(alias.aliasTo());
            }
            if (drop == null) continue;

            MATERIAL_DROPS.put(alias.name(), drop);
            for (ResourceLocation tagId : alias.oreTags()) {
                TAG_DROPS.put(TagKey.create(Registries.BLOCK, tagId), drop);
            }
            addAliasTagMappings(alias.name(), drop);
        }
    }

    private static Item canonicalDrop(MaterialsIndex.MaterialEntry material) {
        ResourceLocation id;
        switch (material.kind()) {
            case "metal" -> id = new ResourceLocation(UnifyWorks.MODID, "raw_" + material.name());
            case "gem" -> id = new ResourceLocation(UnifyWorks.MODID, material.name() + "_gem");
            default -> id = null;
        }
        if (id == null) return null;
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        return item == Items.AIR ? null : item;
    }

    private static void addAliasTagMappings(String name, Item drop) {
        for (String namespace : ORE_NAMESPACES) {
            ResourceLocation tagId = new ResourceLocation(namespace, "ores/" + name);
            TAG_DROPS.putIfAbsent(TagKey.create(Registries.BLOCK, tagId), drop);
        }
    }

    public static Item resolveDrop(Block block) {
        return block.builtInRegistryHolder().tags()
                .map(TAG_DROPS::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Item resolveDrop(String material) {
        if (material == null) return null;
        Item drop = MATERIAL_DROPS.get(material);
        if (drop != null) return drop;
        String canonical = SNAPSHOT.canonicalName(material);
        if (canonical != null && !canonical.equals(material)) {
            drop = MATERIAL_DROPS.get(canonical);
        }
        return drop;
    }

    public static MaterialsIndex.Snapshot snapshot() {
        return SNAPSHOT;
    }

    public static MaterialsIndex.Merge merge() {
        return MERGE;
    }

    public static int compressionTierCap() {
        return MERGED_COMPRESSION_TIER;
    }
}
