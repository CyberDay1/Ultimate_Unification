package com.unifyworks.recipes;

import com.google.gson.JsonObject;
import com.unifyworks.api.CanonicalAPI;
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
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * Wrapper around the vanilla shapeless recipe that canonicalizes its outputs.
 */
public final class CanonicalShapelessRecipe implements CraftingRecipe {
    private final ShapelessRecipe delegate;

    public CanonicalShapelessRecipe(ShapelessRecipe delegate) {
        this.delegate = delegate;
    }

    public ShapelessRecipe delegate() {
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
        return UWRecipes.CANONICAL_SHAPELESS.get();
    }

    @Override
    public RecipeType<?> getType() {
        return delegate.getType();
    }

    @Override
    public boolean isSpecial() {
        return delegate.isSpecial();
    }

    public static final class Serializer implements RecipeSerializer<CanonicalShapelessRecipe> {
        @Override
        public CanonicalShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapelessRecipe vanilla = RecipeSerializer.SHAPELESS_RECIPE.fromJson(id, json);
            return new CanonicalShapelessRecipe(vanilla);
        }

        @Override
        public CanonicalShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            ShapelessRecipe vanilla = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer);
            return new CanonicalShapelessRecipe(vanilla);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CanonicalShapelessRecipe recipe) {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe.delegate());
        }
    }
}
