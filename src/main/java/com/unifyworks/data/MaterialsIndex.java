package com.unifyworks.data;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Minimal bootstrap reader for materials.json before registries freeze. */
public class MaterialsIndex {
    public static class Snapshot {
        public final List<String> metals = new ArrayList<>();
        public final List<String> gems = new ArrayList<>();
        public final List<OreEntry> ores = new ArrayList<>();
        public final List<String> stones = new ArrayList<>();
    }

    public record OreEntry(String name, boolean stone, boolean deepslate, boolean netherrack) {}

    public static Snapshot loadBootstrap() {
        Snapshot snap = new Snapshot();
        try {
            var url = MaterialsIndex.class.getClassLoader().getResource("data/unifyworks/unify/materials.json");
            if (url == null) return snap;
            try (var in = new InputStreamReader(url.openStream())) {
                var root = com.google.gson.JsonParser.parseReader(in).getAsJsonObject();
                var arr = root.getAsJsonArray("materials");
                for (var el : arr) {
                    var obj = el.getAsJsonObject();
                    String name = obj.get("name").getAsString();
                    String kind = obj.get("kind").getAsString();
                    boolean unify = obj.has("unify") && obj.get("unify").getAsBoolean();
                    boolean stone = false, deepslate = false, netherrack = false;
                    if (obj.has("variants")) {
                        var variants = obj.getAsJsonObject("variants");
                        stone = variants.has("stone") && variants.get("stone").getAsBoolean();
                        deepslate = variants.has("deepslate") && variants.get("deepslate").getAsBoolean();
                        netherrack = variants.has("netherrack") && variants.get("netherrack").getAsBoolean();
                    }
                    if (unify && (stone || deepslate || netherrack)) {
                        snap.ores.add(new OreEntry(name, stone, deepslate, netherrack));
                    }
                    boolean nugget = obj.has("provide_nugget") && obj.get("provide_nugget").getAsBoolean();
                    boolean block = obj.has("provide_storage_block") && obj.get("provide_storage_block").getAsBoolean();
                    if ("metal".equals(kind) && (nugget || block)) snap.metals.add(name);
                    if ("gem".equals(kind) && (nugget || block)) snap.gems.add(name);
                    if ("stone".equals(kind) && unify) snap.stones.add(name);
                }
            }
        } catch (Exception ignored) {}
        return snap;
    }
}
