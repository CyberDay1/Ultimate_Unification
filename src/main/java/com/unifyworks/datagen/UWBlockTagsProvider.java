package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import com.unifyworks.registry.UWOres;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.BlockTagsProvider.TagAppender;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.RegistryObject;

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
        var pickaxeMineable = tag(BlockTags.MINEABLE_WITH_PICKAXE);

        for (String metal : snap.metals) {
            var entry = UWBlocks.STORAGE_BLOCKS.get(metal);
            if (entry == null) {
                continue;
            }
            Block block = entry.get();
            forgeStorage.add(block);
            commonStorage.add(block);
            pickaxeMineable.add(block);
            toolRequirementTag(snap.miningFor(metal).blockLevel()).add(block);
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
            pickaxeMineable.add(block);
            toolRequirementTag(snap.miningFor(gem).blockLevel()).add(block);
            tag(blockTag("forge", "storage_blocks/" + gem)).add(block);
            tag(blockTag("c", "storage_blocks/" + gem)).add(block);
        }

        for (MaterialsIndex.OreEntry ore : snap.ores) {
            MaterialsIndex.MiningSpec mining = snap.miningFor(ore.name());
            if (ore.stone()) {
                addOreBlock(pickaxeMineable, mining, UWOres.STONE.get(ore.name()));
            }
            if (ore.deepslate()) {
                addOreBlock(pickaxeMineable, mining, UWOres.DEEPSLATE.get(ore.name()));
            }
            if (ore.netherrack()) {
                addOreBlock(pickaxeMineable, mining, UWOres.NETHERRACK.get(ore.name()));
            }
        }
    }

    private static TagKey<Block> blockTag(String namespace, String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    private TagAppender<Block> toolRequirementTag(String level) {
        return switch (level) {
            case "stone" -> tag(BlockTags.NEEDS_STONE_TOOL);
            case "diamond" -> tag(BlockTags.NEEDS_DIAMOND_TOOL);
            case "netherite" -> tag(BlockTags.NEEDS_NETHERITE_TOOL);
            default -> tag(BlockTags.NEEDS_IRON_TOOL);
        };
    }

    private void addOreBlock(TagAppender<Block> pickaxeMineable, MaterialsIndex.MiningSpec mining,
                             RegistryObject<Block> registryObject) {
        if (registryObject == null) {
            return;
        }
        Block block = registryObject.get();
        pickaxeMineable.add(block);
        toolRequirementTag(mining.oreLevel()).add(block);
    }
}
