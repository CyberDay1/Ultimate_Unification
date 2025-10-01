package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class UWBlockTagsProvider extends BlockTagsProvider {
    public UWBlockTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, UnifyWorks.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var snap = MaterialsIndex.loadBootstrap();
        var forgeStorage = tag(blockTag("forge", "storage_blocks"));
        var commonStorage = tag(blockTag("c", "storage_blocks"));

        for (String metal : snap.metals) {
            var entry = UWBlocks.STORAGE_BLOCKS.get(metal);
            if (entry == null) {
                continue;
            }
            Block block = entry.get();
            forgeStorage.add(block);
            commonStorage.add(block);
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
            tag(blockTag("forge", "storage_blocks/" + metal)).add(block);
            tag(blockTag("c", "storage_blocks/" + metal)).add(block);
        }

        for (String gem : snap.gems) {
            var entry = UWBlocks.STORAGE_BLOCKS.get(gem);
            if (entry == null) {
                continue;
            }
            Block block = entry.get();
            forgeStorage.add(block);
            commonStorage.add(block);
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block);
            tag(blockTag("forge", "storage_blocks/" + gem)).add(block);
            tag(blockTag("c", "storage_blocks/" + gem)).add(block);
        }
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
