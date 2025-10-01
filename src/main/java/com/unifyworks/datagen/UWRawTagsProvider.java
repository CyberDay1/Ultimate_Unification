package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

public class UWRawTagsProvider extends ItemTagsProvider {
    public UWRawTagsProvider(PackOutput out, CompletableFuture<HolderLookup.Provider> lookup, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(out, lookup, blockTags.contentsGetter(), UnifyWorks.MODID, helper);
    }

    private static TagKey<Item> tag(String ns, String path) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, new ResourceLocation(ns, path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var snap = MaterialsIndex.loadBootstrap();
        for (var m : snap.metals) {
            var rawId = new ResourceLocation(UnifyWorks.MODID, "raw_" + m);
            this.tag(tag("forge", "raw_materials/" + m)).addOptional(rawId);
            this.tag(tag("c", "raw_materials/" + m)).addOptional(rawId);
        }
    }
}
