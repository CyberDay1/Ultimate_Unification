package com.unifyworks.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class UWConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    private UWConfig() {}

    public static final class Common {
        private final ModConfigSpec.BooleanValue enableDropUnifier;

        private Common(ModConfigSpec.Builder builder) {
            builder.push("general");
            enableDropUnifier = builder
                    .comment("Enable the global loot modifier that unifies ore drops to the canonical raw chunk or gem.")
                    .define("enableDropUnifier", true);
            builder.pop();
        }

        public boolean dropUnifierEnabled() {
            return enableDropUnifier.get();
        }
    }
}
