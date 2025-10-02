package com.unifyworks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class UWConfig {
    public static final ModConfigSpec COMMON_SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_DROP_UNIFIER;
    private static final ModConfigSpec.BooleanValue ENABLE_COMPRESSION;
    private static final ModConfigSpec.BooleanValue ENABLE_COMPRESSION_METALS;
    private static final ModConfigSpec.BooleanValue ENABLE_COMPRESSION_GEMS;
    private static final ModConfigSpec.BooleanValue ENABLE_COMPRESSION_STONES;
    private static final ModConfigSpec.IntValue MAX_COMPRESSION_TIER;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DENY_MATERIALS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> DENY_STONES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLE_DROP_UNIFIER = builder
                .comment("Enable the global loot modifier that unifies ore drops to the canonical raw chunk or gem.")
                .define("enableDropUnifier", true);
        builder.pop();

        builder.push("compression");
        ENABLE_COMPRESSION = builder.comment("Master toggle for all compression registrations.")
                .define("enable", true);
        ENABLE_COMPRESSION_METALS = builder.comment("Enable compressed metal items and blocks.")
                .define("enableMetals", true);
        ENABLE_COMPRESSION_GEMS = builder.comment("Enable compressed gem items and blocks.")
                .define("enableGems", true);
        ENABLE_COMPRESSION_STONES = builder.comment("Enable compressed stone blocks.")
                .define("enableStones", true);
        MAX_COMPRESSION_TIER = builder.comment("Maximum compression tier (1-9).")
                .defineInRange("maxTier", 9, 1, 9);
        builder.pop();

        builder.push("filters");
        DENY_MATERIALS = builder
                .comment("Blacklist unified metals or gems by id. Entries here are excluded from registration.")
                .defineList("denyMaterials", List.of(), o -> o instanceof String);
        DENY_STONES = builder
                .comment("Blacklist unified stones by id. Entries here are excluded from registration.")
                .defineList("denyStones", List.of(), o -> o instanceof String);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private UWConfig() {}

    public static boolean dropUnifierEnabled() {
        return ENABLE_DROP_UNIFIER.get();
    }

    public static boolean compressionEnabled() {
        return ENABLE_COMPRESSION.get();
    }

    public static boolean compressionMetals() {
        return ENABLE_COMPRESSION_METALS.get();
    }

    public static boolean compressionGems() {
        return ENABLE_COMPRESSION_GEMS.get();
    }

    public static boolean compressionStones() {
        return ENABLE_COMPRESSION_STONES.get();
    }

    public static int maxTier() {
        return MAX_COMPRESSION_TIER.get();
    }

    public static Set<String> denyMaterials() {
        return DENY_MATERIALS.get().stream()
                .map(String::valueOf)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public static Set<String> denyStones() {
        return DENY_STONES.get().stream()
                .map(String::valueOf)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
