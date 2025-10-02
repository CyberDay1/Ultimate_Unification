package com.unifyworks.recipes;

import com.unifyworks.api.CanonicalAPI;
import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/**
 * Wrapper around the vanilla shaped recipe that canonicalizes its outputs when crafted or previewed.
 */
public final class CanonicalShapedRecipe implements CraftingRecipe {
    private final ShapedRecipe delegate;

    public CanonicalShapedRecipe(ShapedRecipe delegate) {
        this.delegate = delegate;
    }

    public ShapedRecipe delegate() {
        return delegate;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return delegate.matches(input, level);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        ItemStack assembled = delegate.assemble(input, provider);
        return CanonicalAPI.canonicalize(assembled);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return CanonicalAPI.preview(delegate.getResultItem(provider));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return delegate.canCraftInDimensions(width, height);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return delegate.getRemainingItems(input);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return delegate.getIngredients();
    }

    @Override
    public ItemStack getToastSymbol() {
        return delegate.getToastSymbol();
    }

    @Override
    public ResourceLocation getId() {
        return delegate.getId();
    }

    @Override
    public String getGroup() {
        return delegate.getGroup();
    }

    @Override
    public CraftingBookCategory category() {
        return delegate.category();
    }

    @Override
    public boolean showNotification() {
        return delegate.showNotification();
    }

    @Override
    public boolean isIncomplete() {
        return delegate.isIncomplete();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UWRecipes.CANONICAL_SHAPED.get();
    }

    @Override
    public RecipeType<?> getType() {
        return delegate.getType();
    }

    @Override
    public boolean isSpecial() {
        return delegate.isSpecial();
    }

    public static final class Serializer implements RecipeSerializer<CanonicalShapedRecipe> {
        @Override
        public CanonicalShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe vanilla = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            return new CanonicalShapedRecipe(vanilla);
        }

        @Override
        public CanonicalShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapedRecipe vanilla = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
            return new CanonicalShapedRecipe(vanilla);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CanonicalShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.delegate());
        }
    }
}
