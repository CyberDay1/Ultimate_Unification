package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import com.unifyworks.registry.UWItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/** Generates 9 nuggets <-> 1 base, and 9 base <-> 1 storage block. */
public class UWRecipeProvider extends RecipeProvider {
    public UWRecipeProvider(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput out) {
        var snap = MaterialsIndex.loadBootstrap();

        for (var m : snap.metals) {
            Item nugget = UWItems.NUGGETS.get(m).get();
            Item ingot = UWItems.BASE_ITEMS.get(m).get();
            Block block = UWBlocks.STORAGE_BLOCKS.get(m).get();

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                    .define('#', nugget).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_nugget", has(nugget))
                    .save(out, UnifyWorks.MODID + ":ingot_from_nuggets/" + m);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                    .requires(ingot)
                    .unlockedBy("has_ingot", has(ingot))
                    .save(out, UnifyWorks.MODID + ":nuggets_from_ingot/" + m);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('#', ingot).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_ingot", has(ingot))
                    .save(out, UnifyWorks.MODID + ":block_from_ingots/" + m);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                    .requires(block)
                    .unlockedBy("has_block", has(block))
                    .save(out, UnifyWorks.MODID + ":ingots_from_block/" + m);
        }

        for (var g : snap.gems) {
            Item nugget = UWItems.NUGGETS.get(g).get();
            Item gem = UWItems.BASE_ITEMS.get(g).get();
            Block block = UWBlocks.STORAGE_BLOCKS.get(g).get();

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gem)
                    .define('#', nugget).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_nugget", has(nugget))
                    .save(out, UnifyWorks.MODID + ":gem_from_nuggets/" + g);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                    .requires(gem)
                    .unlockedBy("has_gem", has(gem))
                    .save(out, UnifyWorks.MODID + ":nuggets_from_gem/" + g);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('#', gem).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_gem", has(gem))
                    .save(out, UnifyWorks.MODID + ":block_from_gems/" + g);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, gem, 9)
                    .requires(block)
                    .unlockedBy("has_block", has(block))
                    .save(out, UnifyWorks.MODID + ":gems_from_block/" + g);
        }
    }
}
