package com.unifyworks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class UWWorldgenConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLE_WORLDGEN;
    private static final ModConfigSpec.BooleanValue ENABLE_OVERWORLD_ORES;
    private static final ModConfigSpec.BooleanValue ENABLE_NETHER_ORES;
    private static final ModConfigSpec.BooleanValue ENABLE_END_ORES;
    private static final ModConfigSpec.IntValue SIZE_MULTIPLIER_PCT;
    private static final ModConfigSpec.IntValue COUNT_MULTIPLIER_PCT;
    private static final ModConfigSpec.BooleanValue PRUNE_NON_UNIFY_ORES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("worldgen");
        ENABLE_WORLDGEN = builder.define("enableWorldgen", true);
        ENABLE_OVERWORLD_ORES = builder.define("enableOverworldOres", true);
        ENABLE_NETHER_ORES = builder.define("enableNetherOres", true);
        ENABLE_END_ORES = builder.define("enableEndOres", false);
        SIZE_MULTIPLIER_PCT = builder.comment("Global vein size multiplier in percent (applied at placement time).")
                .defineInRange("sizeMultiplierPct", 100, 10, 400);
        COUNT_MULTIPLIER_PCT = builder.comment("Global vein count multiplier in percent (applied at placement time).")
                .defineInRange("countMultiplierPct", 100, 10, 400);
        PRUNE_NON_UNIFY_ORES = builder.comment("Remove non-Unify ore features from biomes using removal modifiers.")
                .define("pruneNonUnifyOres", true);
        builder.pop();

        SPEC = builder.build();
    }

    private UWWorldgenConfig() {}

    public static boolean enabled() {
        return ENABLE_WORLDGEN.get();
    }

    public static boolean overworld() {
        return ENABLE_OVERWORLD_ORES.get();
    }

    public static boolean nether() {
        return ENABLE_NETHER_ORES.get();
    }

    public static boolean end() {
        return ENABLE_END_ORES.get();
    }

    public static int sizePct() {
        return SIZE_MULTIPLIER_PCT.get();
    }

    public static int countPct() {
        return COUNT_MULTIPLIER_PCT.get();
    }

    public static boolean prune() {
        return PRUNE_NON_UNIFY_ORES.get();
    }
}
