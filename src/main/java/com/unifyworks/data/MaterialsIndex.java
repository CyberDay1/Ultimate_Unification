package com.unifyworks.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Minimal bootstrap reader for materials.json before registries freeze. */
public class MaterialsIndex {
    public static class Snapshot {
        public final List<String> metals = new ArrayList<>();
        public final List<String> gems = new ArrayList<>();
        public final List<OreEntry> ores = new ArrayList<>();
        public final List<String> stones = new ArrayList<>();

        private final Map<String, MaterialEntry> materials = new LinkedHashMap<>();
        private final Map<String, String> aliasToCanonical = new LinkedHashMap<>();
        private final List<AliasEntry> oreAliases = new ArrayList<>();

        public List<MaterialEntry> materials() {
            return List.copyOf(materials.values());
        }

        public List<AliasEntry> oreAliases() {
            return List.copyOf(oreAliases);
        }

        public MaterialEntry find(String name) {
            String canonical = canonicalName(name);
            return canonical == null ? null : materials.get(canonical);
        }

        public String canonicalName(String name) {
            return aliasToCanonical.get(name);
        }

        public Set<String> allMaterialNames() {
            return Set.copyOf(materials.keySet());
        }

        public Set<String> allNamesWithAliases() {
            return Set.copyOf(aliasToCanonical.keySet());
        }

        void addMaterial(MaterialEntry entry) {
            materials.put(entry.name(), entry);
            aliasToCanonical.put(entry.name(), entry.name());
            for (String alias : entry.aliases()) {
                aliasToCanonical.put(alias, entry.name());
            }
        }

        void addOreAlias(AliasEntry entry) {
            oreAliases.add(entry);
            if (entry.aliasTo() != null && !entry.aliasTo().isEmpty()) {
                aliasToCanonical.put(entry.name(), entry.aliasTo());
            }
        }

        void addOreEntry(OreEntry entry) {
            ores.removeIf(existing -> existing.name().equals(entry.name()));
            ores.add(entry);
        }

        public void merge(Snapshot other) {
            for (MaterialEntry entry : other.materials.values()) {
                addMaterial(entry);
            }
            for (AliasEntry alias : other.oreAliases) {
                addOreAlias(alias);
            }
            for (String metal : other.metals) {
                addUnique(metals, metal);
            }
            for (String gem : other.gems) {
                addUnique(gems, gem);
            }
            for (String stone : other.stones) {
                addUnique(stones, stone);
            }
            for (OreEntry ore : other.ores) {
                addOreEntry(ore);
            }
        }
    }

    public record OreEntry(String name, boolean stone, boolean deepslate, boolean netherrack) {}

    public record MaterialEntry(String name, String kind, boolean unify, boolean generateOre,
                                List<String> aliases, List<ResourceLocation> oreTags) {
        public List<String> namesWithAliases() {
            List<String> names = new ArrayList<>(1 + aliases.size());
            names.add(name);
            names.addAll(aliases);
            return List.copyOf(names);
        }
    }

    public record AliasEntry(String name, String aliasTo, List<ResourceLocation> oreTags) {}

    public static Snapshot loadBootstrap() {
        try {
            var url = MaterialsIndex.class.getClassLoader().getResource("data/unifyworks/unify/materials.json");
            if (url == null) return new Snapshot();
            try (var in = new InputStreamReader(url.openStream())) {
                JsonElement root = JsonParser.parseReader(in);
                return fromJson(root);
            }
        } catch (Exception ignored) {}
        return new Snapshot();
    }

    public static Snapshot fromJson(JsonElement root) {
        Snapshot snap = new Snapshot();
        if (root == null || !root.isJsonObject()) return snap;
        JsonObject obj = root.getAsJsonObject();
        JsonArray materials = obj.getAsJsonArray("materials");
        if (materials == null) return snap;

        for (JsonElement element : materials) {
            if (!element.isJsonObject()) continue;
            JsonObject material = element.getAsJsonObject();
            parseMaterial(material, snap);
        }
        return snap;
    }

    private static void parseMaterial(JsonObject material, Snapshot snap) {
        String name = asString(material, "name");
        if (name == null || name.isEmpty()) return;
        String kind = asString(material, "kind");
        boolean unify = material.has("unify") && material.get("unify").getAsBoolean();

        if ("ore_alias".equals(kind)) {
            String aliasTo = asString(material, "alias_to");
            List<ResourceLocation> tags = readTagList(material);
            snap.addOreAlias(new AliasEntry(name, aliasTo, tags));
            return;
        }

        boolean generateOre = material.has("generate_ore") && material.get("generate_ore").getAsBoolean();
        List<String> aliases = readStringList(material, "aliases");
        List<ResourceLocation> oreTags = readTagList(material, "ore");
        snap.addMaterial(new MaterialEntry(name, kind, unify, generateOre, aliases, oreTags));

        boolean provideNugget = material.has("provide_nugget") && material.get("provide_nugget").getAsBoolean();
        boolean provideStorageBlock = material.has("provide_storage_block") && material.get("provide_storage_block").getAsBoolean();

        if ("metal".equals(kind) && unify && (provideNugget || provideStorageBlock)) {
            addUnique(snap.metals, name);
        }
        if ("gem".equals(kind) && unify && (provideNugget || provideStorageBlock)) {
            addUnique(snap.gems, name);
        }
        if ("stone".equals(kind) && unify) {
            addUnique(snap.stones, name);
        }

        JsonObject variants = material.has("variants") && material.get("variants").isJsonObject()
                ? material.getAsJsonObject("variants") : null;
        boolean stoneVariant = variantEnabled(variants, "stone");
        boolean deepslateVariant = variantEnabled(variants, "deepslate");
        boolean netherVariant = variantEnabled(variants, "netherrack");
        if (unify && (stoneVariant || deepslateVariant || netherVariant)) {
            snap.addOreEntry(new OreEntry(name, stoneVariant, deepslateVariant, netherVariant));
        }
    }

    private static boolean variantEnabled(JsonObject variants, String key) {
        return variants != null && variants.has(key) && variants.get(key).getAsBoolean();
    }

    private static List<ResourceLocation> readTagList(JsonObject material, String... keys) {
        if (!material.has("tags") || !material.get("tags").isJsonObject()) return List.of();
        JsonObject tags = material.getAsJsonObject("tags");
        LinkedHashSet<ResourceLocation> results = new LinkedHashSet<>();
        if (keys.length == 0) {
            for (Map.Entry<String, JsonElement> entry : tags.entrySet()) {
                collectTagArray(entry.getValue(), results);
            }
        } else {
            for (String key : keys) {
                if (!tags.has(key)) continue;
                collectTagArray(tags.get(key), results);
            }
        }
        return List.copyOf(results);
    }

    private static void collectTagArray(JsonElement element, Set<ResourceLocation> sink) {
        if (!element.isJsonArray()) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) continue;
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id != null) sink.add(id);
        }
    }

    private static List<String> readStringList(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonArray()) return List.of();
        JsonArray array = obj.getAsJsonArray(key);
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                values.add(element.getAsString());
            }
        }
        return List.copyOf(values);
    }

    private static String asString(JsonObject obj, String key) {
        if (!obj.has(key)) return null;
        JsonElement element = obj.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) return null;
        return element.getAsString();
    }

    private static void addUnique(List<String> list, String value) {
        if (!list.contains(value)) {
            list.add(value);
        }
    }
}
