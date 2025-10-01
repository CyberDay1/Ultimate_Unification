package com.unifyworks.data;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Minimal bootstrap reader for materials.json before registries freeze. */
public class MaterialsIndex {
    public static class Snapshot {
        public final List<String> metals = new ArrayList<>();
        public final List<String> gems = new ArrayList<>();
    }

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
                    boolean nugget = obj.has("provide_nugget") && obj.get("provide_nugget").getAsBoolean();
                    boolean block = obj.has("provide_storage_block") && obj.get("provide_storage_block").getAsBoolean();
                    if ("metal".equals(kind) && (nugget || block)) snap.metals.add(name);
                    if ("gem".equals(kind) && (nugget || block)) snap.gems.add(name);
                }
            }
        } catch (Exception ignored) {}
        return snap;
    }
}
