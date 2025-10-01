package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import com.unifyworks.registry.UWItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class UWItemTagsProvider extends ItemTagsProvider {
    public UWItemTagsProvider(PackOutput output,
                              CompletableFuture<HolderLookup.Provider> lookupProvider,
                              UWBlockTagsProvider blockTags,
                              ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags.contentsGetter(), UnifyWorks.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var snap = MaterialsIndex.loadBootstrap();

        var forgeNuggets = tag(itemTag("forge", "nuggets"));
        var commonNuggets = tag(itemTag("c", "nuggets"));
        var forgeIngots = tag(itemTag("forge", "ingots"));
        var commonIngots = tag(itemTag("c", "ingots"));
        var forgeGems = tag(itemTag("forge", "gems"));
        var commonGems = tag(itemTag("c", "gems"));
        var forgeStorage = tag(itemTag("forge", "storage_blocks"));
        var commonStorage = tag(itemTag("c", "storage_blocks"));

        for (String metal : snap.metals) {
            var nuggetEntry = UWItems.NUGGETS.get(metal);
            var baseEntry = UWItems.BASE_ITEMS.get(metal);
            var blockEntry = UWBlocks.STORAGE_BLOCKS.get(metal);
            if (nuggetEntry == null || baseEntry == null || blockEntry == null) {
                continue;
            }
            Item nugget = nuggetEntry.get();
            Item ingot = baseEntry.get();
            Item storageBlock = blockEntry.get().asItem();

            forgeNuggets.add(nugget);
            commonNuggets.add(nugget);
            forgeIngots.add(ingot);
            commonIngots.add(ingot);
            forgeStorage.add(storageBlock);
            commonStorage.add(storageBlock);

            tag(itemTag("forge", "nuggets/" + metal)).add(nugget);
            tag(itemTag("c", "nuggets/" + metal)).add(nugget);
            tag(itemTag("forge", "ingots/" + metal)).add(ingot);
            tag(itemTag("c", "ingots/" + metal)).add(ingot);
            tag(itemTag("forge", "storage_blocks/" + metal)).add(storageBlock);
            tag(itemTag("c", "storage_blocks/" + metal)).add(storageBlock);
        }

        for (String gem : snap.gems) {
            var nuggetEntry = UWItems.NUGGETS.get(gem);
            var baseEntry = UWItems.BASE_ITEMS.get(gem);
            var blockEntry = UWBlocks.STORAGE_BLOCKS.get(gem);
            if (nuggetEntry == null || baseEntry == null || blockEntry == null) {
                continue;
            }
            Item nugget = nuggetEntry.get();
            Item gemItem = baseEntry.get();
            Item storageBlock = blockEntry.get().asItem();

            forgeNuggets.add(nugget);
            commonNuggets.add(nugget);
            forgeGems.add(gemItem);
            commonGems.add(gemItem);
            forgeStorage.add(storageBlock);
            commonStorage.add(storageBlock);

            tag(itemTag("forge", "nuggets/" + gem)).add(nugget);
            tag(itemTag("c", "nuggets/" + gem)).add(nugget);
            tag(itemTag("forge", "gems/" + gem)).add(gemItem);
            tag(itemTag("c", "gems/" + gem)).add(gemItem);
            tag(itemTag("forge", "storage_blocks/" + gem)).add(storageBlock);
            tag(itemTag("c", "storage_blocks/" + gem)).add(storageBlock);
        }
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
