package com.unifyworks.recipes;

import com.unifyworks.UnifyWorks;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

/**
 * Registration hub for UnifyWorks recipe serializers.
 */
public final class UWRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.RECIPE_SERIALIZERS, UnifyWorks.MODID);

    public static final RegistryObject<RecipeSerializer<CanonicalShapedRecipe>> CANONICAL_SHAPED =
            SERIALIZERS.register("canonical_shaped", CanonicalShapedRecipe.Serializer::new);

    public static final RegistryObject<RecipeSerializer<CanonicalShapelessRecipe>> CANONICAL_SHAPELESS =
            SERIALIZERS.register("canonical_shapeless", CanonicalShapelessRecipe.Serializer::new);

    private UWRecipes() {
    }
}
