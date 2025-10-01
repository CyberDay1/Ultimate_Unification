package com.unifyworks.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.util.Map;

public class MaterialsLoader implements ResourceManagerReloadListener {
    public static final ResourceLocation MATERIALS = ResourceLocation.fromNamespaceAndPath("unifyworks", "unify/materials.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static MaterialsSpec SPEC = new MaterialsSpec();

    @Override
    public void onResourceManagerReload(ResourceManager mgr) {
        mgr.getResource(MATERIALS).ifPresentOrElse(res -> {
            try (var in = new InputStreamReader(res.open())) {
                SPEC = GSON.fromJson(in, MaterialsSpec.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load materials.json", e);
            }
        }, () -> {
            SPEC = new MaterialsSpec(); // empty fallback
        });
    }

    public static class MaterialsSpec {
        public int version = 1;
        public java.util.List<Map<String,Object>> materials = java.util.List.of();
        public Map<String,Object> compression = java.util.Map.of();
        public Map<String,Object> worldgen = java.util.Map.of();
    }
}
