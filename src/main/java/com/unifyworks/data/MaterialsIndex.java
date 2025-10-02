package com.unifyworks.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.unifyworks.UnifyWorks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        private final Map<String, MiningSpec> miningSpecs = new LinkedHashMap<>();
        private final Map<String, OreEntry> oreByName = new LinkedHashMap<>();

        private int maxCompressionTier = 9;

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
            oreByName.put(entry.name(), entry);
        }

        public void merge(Snapshot other) {
            for (MaterialEntry entry : other.materials.values()) {
                addMaterial(entry);
            }
            for (AliasEntry alias : other.oreAliases) {
                addOreAlias(alias);
            }
            miningSpecs.putAll(other.miningSpecs);
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
            maxCompressionTier = Math.min(maxCompressionTier, other.maxCompressionTier());
        }

        public Snapshot filtered(Set<String> denyMaterials, Set<String> denyStones) {
            Set<String> blockedMaterials = denyMaterials == null ? Set.of() : denyMaterials;
            Set<String> blockedStones = denyStones == null ? Set.of() : denyStones;

            Snapshot filtered = new Snapshot();

            filtered.maxCompressionTier = maxCompressionTier;

            for (MaterialEntry entry : materials.values()) {
                if (blockedMaterials.contains(entry.name())) continue;
                if ("stone".equals(entry.kind()) && blockedStones.contains(entry.name())) continue;
                filtered.addMaterial(entry);
                filtered.addMining(entry.name(), miningFor(entry.name()));
                if ("metal".equals(entry.kind()) && entry.unify() && (entry.provideNugget() || entry.provideStorageBlock())) {
                    addUnique(filtered.metals, entry.name());
                }
                if ("gem".equals(entry.kind()) && entry.unify() && (entry.provideNugget() || entry.provideStorageBlock())) {
                    addUnique(filtered.gems, entry.name());
                }
                if ("stone".equals(entry.kind()) && entry.unify()) {
                    addUnique(filtered.stones, entry.name());
                }
                OreEntry oreEntry = oreByName.get(entry.name());
                if (oreEntry != null && (oreEntry.stone() || oreEntry.deepslate() || oreEntry.netherrack())) {
                    filtered.addOreEntry(oreEntry);
                }
            }

            for (AliasEntry alias : oreAliases) {
                if (blockedMaterials.contains(alias.name())) continue;
                String aliasTo = alias.aliasTo();
                if (aliasTo != null) {
                    String canonical = canonicalName(aliasTo);
                    if (canonical != null && blockedMaterials.contains(canonical)) continue;
                    if (blockedMaterials.contains(aliasTo)) continue;
                }
                filtered.addOreAlias(alias);
            }

            return filtered;
        }

        public Set<String> allMaterials() {
            Set<String> all = new LinkedHashSet<>(metals);
            all.addAll(gems);
            return Set.copyOf(all);
        }

        public MiningSpec miningFor(String name) {
            if (name == null || name.isEmpty()) {
                return MiningSpec.DEFAULT;
            }
            String canonical = canonicalName(name);
            if (canonical != null) {
                MiningSpec spec = miningSpecs.get(canonical);
                if (spec != null) {
                    return spec;
                }
            }
            MiningSpec direct = miningSpecs.get(name);
            return direct != null ? direct : MiningSpec.DEFAULT;
        }

        void addMining(String name, MiningSpec spec) {
            miningSpecs.put(name, spec == null ? MiningSpec.DEFAULT : spec);
        }

        public OreEntry oreEntryFor(String name) {
            return oreByName.get(name);
        }

        public int maxCompressionTier() {
            return maxCompressionTier;
        }

        void setMaxCompressionTier(int tier) {
            this.maxCompressionTier = tier;
        }

        public Snapshot applyMerge(Merge merge) {
            if (merge == null) {
                Snapshot clone = new Snapshot();
                clone.merge(this);
                clone.setMaxCompressionTier(maxCompressionTier);
                return clone;
            }

            Snapshot merged = new Snapshot();
            merged.setMaxCompressionTier(merge.maxTierOverride > 0
                    ? Math.min(maxCompressionTier, merge.maxTierOverride)
                    : maxCompressionTier);

            Set<String> blockedMaterials = merge.denyMaterials;
            Set<String> blockedStones = merge.denyStones;

            for (MaterialEntry entry : materials.values()) {
                if (blockedMaterials.contains(entry.name())) continue;
                if ("stone".equals(entry.kind()) && blockedStones.contains(entry.name())) continue;

                MaterialOverride override = merge.overrides.get(entry.name());
                MaterialEntry applied = override != null ? override.apply(entry) : entry;
                MiningSpec mining = override != null ? override.applyMining(miningFor(entry.name())) : miningFor(entry.name());
                OreEntry baseOre = oreEntryFor(entry.name());
                OreEntry oreEntry = override != null ? override.applyVariants(applied, baseOre) : baseOre;

                merged.addMaterial(applied);
                merged.addMining(applied.name(), mining);

                if ("metal".equals(applied.kind()) && applied.unify() && (applied.provideNugget() || applied.provideStorageBlock())) {
                    addUnique(merged.metals, applied.name());
                }
                if ("gem".equals(applied.kind()) && applied.unify() && (applied.provideNugget() || applied.provideStorageBlock())) {
                    addUnique(merged.gems, applied.name());
                }
                if ("stone".equals(applied.kind()) && applied.unify()) {
                    addUnique(merged.stones, applied.name());
                }

                if (oreEntry != null && applied.unify() && (oreEntry.stone() || oreEntry.deepslate() || oreEntry.netherrack())) {
                    merged.addOreEntry(oreEntry);
                }
            }

            for (AliasEntry alias : oreAliases) {
                if (blockedMaterials.contains(alias.name())) continue;
                String aliasTo = alias.aliasTo();
                if (aliasTo != null) {
                    String canonical = canonicalName(aliasTo);
                    if (canonical != null && blockedMaterials.contains(canonical)) continue;
                    if (blockedMaterials.contains(aliasTo)) continue;
                }
                if (alias.aliasTo() != null && merged.find(alias.aliasTo()) == null) continue;
                merged.addOreAlias(alias);
            }

            return merged;
        }
    }

    public record OreEntry(String name, boolean stone, boolean deepslate, boolean netherrack) {}

    public record MaterialEntry(String name, String kind, boolean unify, boolean generateOre,
                                boolean provideNugget, boolean provideStorageBlock,
                                List<String> aliases, List<ResourceLocation> oreTags) {
        public List<String> namesWithAliases() {
            List<String> names = new ArrayList<>(1 + aliases.size());
            names.add(name);
            names.addAll(aliases);
            return List.copyOf(names);
        }
    }

    public static final class Merge {
        public final Set<String> denyMaterials = new LinkedHashSet<>();
        public final Set<String> denyStones = new LinkedHashSet<>();
        public int maxTierOverride = -1;
        public final Map<String, MaterialOverride> overrides = new LinkedHashMap<>();
    }

    public record MaterialOverride(Boolean unify, Boolean generateOre, Boolean provideNugget, Boolean provideStorageBlock,
                                   List<String> aliasesAdd, MiningOverride mining, VariantsOverride variants) {
        public MaterialEntry apply(MaterialEntry base) {
            boolean newUnify = unify != null ? unify : base.unify();
            boolean newGenerate = generateOre != null ? generateOre : base.generateOre();
            boolean newProvideNugget = provideNugget != null ? provideNugget : base.provideNugget();
            boolean newProvideStorage = provideStorageBlock != null ? provideStorageBlock : base.provideStorageBlock();

            List<String> aliases = new ArrayList<>(base.aliases());
            if (aliasesAdd != null) {
                for (String alias : aliasesAdd) {
                    if (!aliases.contains(alias) && !Objects.equals(alias, base.name())) {
                        aliases.add(alias);
                    }
                }
            }
            return new MaterialEntry(base.name(), base.kind(), newUnify, newGenerate, newProvideNugget, newProvideStorage,
                    List.copyOf(aliases), base.oreTags());
        }

        public MiningSpec applyMining(MiningSpec base) {
            if (mining == null) return base;
            String oreLevel = mining.oreLevel != null ? mining.oreLevel : base.oreLevel();
            String blockLevel = mining.blockLevel != null ? mining.blockLevel : base.blockLevel();
            float oreHardness = mining.oreHardness != null ? mining.oreHardness : base.oreHardness();
            float blockHardness = mining.blockHardness != null ? mining.blockHardness : base.blockHardness();
            return new MiningSpec(oreLevel, blockLevel, oreHardness, blockHardness);
        }

        public OreEntry applyVariants(MaterialEntry applied, OreEntry base) {
            if (variants == null && base == null) {
                return null;
            }
            if (!applied.unify()) {
                return null;
            }

            boolean stone = base != null && base.stone();
            boolean deepslate = base != null && base.deepslate();
            boolean nether = base != null && base.netherrack();

            if (variants != null) {
                if (variants.stone != null) stone = variants.stone;
                if (variants.deepslate != null) deepslate = variants.deepslate;
                if (variants.netherrack != null) nether = variants.netherrack;
            }

            if (!stone && !deepslate && !nether) {
                return null;
            }
            return new OreEntry(applied.name(), stone, deepslate, nether);
        }
    }

    public static final class MiningOverride {
        final String oreLevel;
        final String blockLevel;
        final Float oreHardness;
        final Float blockHardness;

        public MiningOverride(String oreLevel, String blockLevel, Float oreHardness, Float blockHardness) {
            this.oreLevel = oreLevel;
            this.blockLevel = blockLevel;
            this.oreHardness = oreHardness;
            this.blockHardness = blockHardness;
        }
    }

    public static final class VariantsOverride {
        final Boolean stone;
        final Boolean deepslate;
        final Boolean netherrack;

        public VariantsOverride(Boolean stone, Boolean deepslate, Boolean netherrack) {
            this.stone = stone;
            this.deepslate = deepslate;
            this.netherrack = netherrack;
        }
    }

    private static List<String> readStringArray(JsonArray array) {
        List<String> values = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        }
        return List.copyOf(values);
    }

    private static Boolean readBooleanOverride(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        var primitive = obj.get(key).getAsJsonPrimitive();
        if (!primitive.isBoolean()) {
            return null;
        }
        return primitive.getAsBoolean();
    }

    private static String readToolOverride(JsonObject obj, String key) {
        String value = asString(obj, key);
        if (value == null) return null;
        return TOOL_LEVELS.contains(value) ? value : null;
    }

    private static Float readFloatOverride(JsonObject obj, String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return null;
        }
        var primitive = obj.get(key).getAsJsonPrimitive();
        if (!primitive.isNumber()) {
            return null;
        }
        try {
            return primitive.getAsFloat();
        } catch (NumberFormatException ignored) {
            return null;
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

    public static Merge loadMergeLayer(ResourceManager rm) {
        Merge merge = new Merge();
        if (rm == null) {
            return merge;
        }

        try {
            Map<ResourceLocation, Resource> filterResources = rm.listResources("unify", rl -> rl.getPath().endsWith("filters.json"));
            List<Map.Entry<ResourceLocation, Resource>> sortedFilters = new ArrayList<>(filterResources.entrySet());
            sortedFilters.sort(Comparator
                    .comparing((Map.Entry<ResourceLocation, Resource> e) -> e.getKey().getNamespace())
                    .thenComparing(e -> e.getKey().getPath()));
            for (Map.Entry<ResourceLocation, Resource> entry : sortedFilters) {
                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    int version = json.has("version") ? json.get("version").getAsInt() : 1;
                    if (version != 1) continue;
                    if (json.has("deny_materials") && json.get("deny_materials").isJsonArray()) {
                        JsonArray array = json.getAsJsonArray("deny_materials");
                        array.forEach(el -> {
                            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                                merge.denyMaterials.add(el.getAsString());
                            }
                        });
                    }
                    if (json.has("deny_stones") && json.get("deny_stones").isJsonArray()) {
                        JsonArray array = json.getAsJsonArray("deny_stones");
                        array.forEach(el -> {
                            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                                merge.denyStones.add(el.getAsString());
                            }
                        });
                    }
                    if (json.has("max_compression_tier") && json.get("max_compression_tier").isJsonPrimitive()) {
                        try {
                            int tier = json.get("max_compression_tier").getAsInt();
                            merge.maxTierOverride = Math.max(1, Math.min(9, tier));
                        } catch (NumberFormatException ignored) {}
                    }
                } catch (Exception ex) {
                    UnifyWorks.LOGGER.warn("Failed to parse UnifyWorks filters {}", entry.getKey(), ex);
                }
            }
        } catch (Exception ex) {
            UnifyWorks.LOGGER.warn("Failed to enumerate UnifyWorks filters", ex);
        }

        try {
            Map<ResourceLocation, Resource> overrideResources = rm.listResources("unify/overrides", rl -> rl.getPath().endsWith(".json"));
            List<Map.Entry<ResourceLocation, Resource>> sortedOverrides = new ArrayList<>(overrideResources.entrySet());
            sortedOverrides.sort(Comparator
                    .comparing((Map.Entry<ResourceLocation, Resource> e) -> e.getKey().getNamespace())
                    .thenComparing(e -> e.getKey().getPath()));
            for (Map.Entry<ResourceLocation, Resource> entry : sortedOverrides) {
                try (var reader = new InputStreamReader(entry.getValue().open())) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    int version = json.has("version") ? json.get("version").getAsInt() : 1;
                    if (version != 1) continue;
                    String name = asString(json, "name");
                    if (name == null || name.isEmpty()) continue;

                    Boolean unify = json.has("unify") && json.get("unify").isJsonPrimitive() && json.get("unify").getAsJsonPrimitive().isBoolean()
                            ? json.get("unify").getAsBoolean() : null;
                    Boolean generateOre = json.has("generate_ore") && json.get("generate_ore").isJsonPrimitive() && json.get("generate_ore").getAsJsonPrimitive().isBoolean()
                            ? json.get("generate_ore").getAsBoolean() : null;
                    Boolean provideNugget = json.has("provide_nugget") && json.get("provide_nugget").isJsonPrimitive() && json.get("provide_nugget").getAsJsonPrimitive().isBoolean()
                            ? json.get("provide_nugget").getAsBoolean() : null;
                    Boolean provideStorage = json.has("provide_storage_block") && json.get("provide_storage_block").isJsonPrimitive() && json.get("provide_storage_block").getAsJsonPrimitive().isBoolean()
                            ? json.get("provide_storage_block").getAsBoolean() : null;

                    List<String> aliasesAdd = json.has("aliases_add") && json.get("aliases_add").isJsonArray()
                            ? readStringArray(json.getAsJsonArray("aliases_add")) : List.of();

                    MiningOverride mining = null;
                    if (json.has("mining") && json.get("mining").isJsonObject()) {
                        JsonObject miningObj = json.getAsJsonObject("mining");
                        String oreLevel = readToolOverride(miningObj, "ore_level");
                        String blockLevel = readToolOverride(miningObj, "block_level");
                        Float oreHardness = readFloatOverride(miningObj, "ore_hardness");
                        Float blockHardness = readFloatOverride(miningObj, "block_hardness");
                        mining = new MiningOverride(oreLevel, blockLevel, oreHardness, blockHardness);
                    }

                    VariantsOverride variants = null;
                    if (json.has("variants") && json.get("variants").isJsonObject()) {
                        JsonObject variantsObj = json.getAsJsonObject("variants");
                        Boolean stone = readBooleanOverride(variantsObj, "stone");
                        Boolean deepslate = readBooleanOverride(variantsObj, "deepslate");
                        Boolean netherrack = readBooleanOverride(variantsObj, "netherrack");
                        variants = new VariantsOverride(stone, deepslate, netherrack);
                    }

                    MaterialOverride override = new MaterialOverride(unify, generateOre, provideNugget, provideStorage, aliasesAdd, mining, variants);
                    merge.overrides.put(name, override);
                } catch (Exception ex) {
                    UnifyWorks.LOGGER.warn("Failed to parse UnifyWorks override {}", entry.getKey(), ex);
                }
            }
        } catch (Exception ex) {
            UnifyWorks.LOGGER.warn("Failed to enumerate UnifyWorks overrides", ex);
        }

        return merge;
    }

    public static Snapshot fromJson(JsonElement root) {
        Snapshot snap = new Snapshot();
        if (root == null || !root.isJsonObject()) return snap;
        JsonObject obj = root.getAsJsonObject();
        if (obj.has("compression") && obj.get("compression").isJsonObject()) {
            JsonObject compression = obj.getAsJsonObject("compression");
            if (compression.has("max_tier") && compression.get("max_tier").isJsonPrimitive()) {
                try {
                    int tier = compression.get("max_tier").getAsInt();
                    snap.setMaxCompressionTier(Math.max(1, Math.min(9, tier)));
                } catch (NumberFormatException ignored) {}
            }
        }

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
        boolean provideNugget = material.has("provide_nugget") && material.get("provide_nugget").getAsBoolean();
        boolean provideStorageBlock = material.has("provide_storage_block") && material.get("provide_storage_block").getAsBoolean();
        List<String> aliases = readStringList(material, "aliases");
        List<ResourceLocation> oreTags = readTagList(material, "ore");
        snap.addMaterial(new MaterialEntry(name, kind, unify, generateOre, provideNugget, provideStorageBlock, aliases, oreTags));
        snap.addMining(name, readMiningSpec(material));

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

    public static final class MiningSpec {
        public static final MiningSpec DEFAULT = new MiningSpec("iron", "stone", 3.0f, 5.0f);

        private final String oreLevel;
        private final String blockLevel;
        private final float oreHardness;
        private final float blockHardness;

        public MiningSpec(String oreLevel, String blockLevel, float oreHardness, float blockHardness) {
            this.oreLevel = oreLevel;
            this.blockLevel = blockLevel;
            this.oreHardness = oreHardness;
            this.blockHardness = blockHardness;
        }

        public String oreLevel() {
            return oreLevel;
        }

        public String blockLevel() {
            return blockLevel;
        }

        public float oreHardness() {
            return oreHardness;
        }

        public float blockHardness() {
            return blockHardness;
        }
    }

    private static final Set<String> TOOL_LEVELS = Set.of("stone", "iron", "diamond", "netherite");

    private static MiningSpec readMiningSpec(JsonObject material) {
        JsonObject mining = material.has("mining") && material.get("mining").isJsonObject()
                ? material.getAsJsonObject("mining") : null;

        if (mining == null) {
            return MiningSpec.DEFAULT;
        }

        String oreLevel = parseToolLevel(mining, "ore_level", MiningSpec.DEFAULT.oreLevel());
        String blockLevel = parseToolLevel(mining, "block_level", MiningSpec.DEFAULT.blockLevel());
        float oreHardness = parseFloat(mining, "ore_hardness", MiningSpec.DEFAULT.oreHardness());
        float blockHardness = parseFloat(mining, "block_hardness", MiningSpec.DEFAULT.blockHardness());
        return new MiningSpec(oreLevel, blockLevel, oreHardness, blockHardness);
    }

    private static String parseToolLevel(JsonObject mining, String key, String fallback) {
        if (!mining.has(key)) {
            return fallback;
        }
        JsonElement element = mining.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
            return fallback;
        }
        String value = element.getAsString();
        return TOOL_LEVELS.contains(value) ? value : fallback;
    }

    private static float parseFloat(JsonObject obj, String key, float fallback) {
        if (!obj.has(key)) {
            return fallback;
        }
        JsonElement element = obj.get(key);
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            return fallback;
        }
        try {
            return element.getAsFloat();
        } catch (NumberFormatException ex) {
            return fallback;
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
