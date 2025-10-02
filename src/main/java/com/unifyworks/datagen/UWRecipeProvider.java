package com.unifyworks.datagen;

import com.unifyworks.UnifyWorks;
import com.unifyworks.data.MaterialsIndex;
import com.unifyworks.registry.UWBlocks;
import com.unifyworks.registry.UWItems;
import com.unifyworks.recipes.CanonicalShapedRecipe;
import com.unifyworks.recipes.CanonicalShapelessRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/** Generates 9 nuggets <-> 1 base, and 9 base <-> 1 storage block. */
public class UWRecipeProvider extends RecipeProvider {
    private final boolean canonicalSerializers;

    public UWRecipeProvider(PackOutput output, CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries) {
        this(output, registries, false);
    }

    public UWRecipeProvider(PackOutput output,
                            CompletableFuture<net.minecraft.core.HolderLookup.Provider> registries,
                            boolean canonicalSerializers) {
        super(output, registries);
        this.canonicalSerializers = canonicalSerializers;
    }

    @Override
    protected void buildRecipes(RecipeOutput out) {
        var snap = MaterialsIndex.loadBootstrap();
        RecipeOutput target = canonicalSerializers ? canonicalize(out) : out;

        for (var m : snap.metals) {
            var nuggetEntry = UWItems.NUGGETS.get(m);
            var baseEntry = UWItems.BASE_ITEMS.get(m);
            var blockEntry = UWBlocks.STORAGE_BLOCKS.get(m);
            if (nuggetEntry == null || baseEntry == null || blockEntry == null) {
                continue;
            }
            Item nugget = nuggetEntry.get();
            Item ingot = baseEntry.get();
            Block block = blockEntry.get();

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ingot)
                    .define('#', nugget).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_nugget", has(nugget))
                    .save(target, UnifyWorks.MODID + ":ingot_from_nuggets/" + m);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                    .requires(ingot)
                    .unlockedBy("has_ingot", has(ingot))
                    .save(target, UnifyWorks.MODID + ":nuggets_from_ingot/" + m);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('#', ingot).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_ingot", has(ingot))
                    .save(target, UnifyWorks.MODID + ":block_from_ingots/" + m);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ingot, 9)
                    .requires(block)
                    .unlockedBy("has_block", has(block))
                    .save(target, UnifyWorks.MODID + ":ingots_from_block/" + m);
        }

        for (var g : snap.gems) {
            var nuggetEntry = UWItems.NUGGETS.get(g);
            var baseEntry = UWItems.BASE_ITEMS.get(g);
            var blockEntry = UWBlocks.STORAGE_BLOCKS.get(g);
            if (nuggetEntry == null || baseEntry == null || blockEntry == null) {
                continue;
            }
            Item nugget = nuggetEntry.get();
            Item gem = baseEntry.get();
            Block block = blockEntry.get();

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, gem)
                    .define('#', nugget).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_nugget", has(nugget))
                    .save(target, UnifyWorks.MODID + ":gem_from_nuggets/" + g);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nugget, 9)
                    .requires(gem)
                    .unlockedBy("has_gem", has(gem))
                    .save(target, UnifyWorks.MODID + ":nuggets_from_gem/" + g);

            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                    .define('#', gem).pattern("###").pattern("###").pattern("###")
                    .unlockedBy("has_gem", has(gem))
                    .save(target, UnifyWorks.MODID + ":block_from_gems/" + g);

            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, gem, 9)
                    .requires(block)
                    .unlockedBy("has_block", has(block))
                    .save(target, UnifyWorks.MODID + ":gems_from_block/" + g);
        }
    }

    private RecipeOutput canonicalize(RecipeOutput delegate) {
        return new RecipeOutput() {
            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, AdvancementHolder advancement) {
                if (recipe instanceof ShapedRecipe shaped) {
                    delegate.accept(id, new CanonicalShapedRecipe(shaped), advancement);
                    return;
                }
                if (recipe instanceof ShapelessRecipe shapeless) {
                    delegate.accept(id, new CanonicalShapelessRecipe(shapeless), advancement);
                    return;
                }
                delegate.accept(id, recipe, advancement);
            }

            @Override
            public Advancement.Builder advancement() {
                return delegate.advancement();
            }
        };
    }
}
