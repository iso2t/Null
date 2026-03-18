package net.iso2t.nullmod.api.data;

import net.minecraft.data.recipes.RecipeOutput;
import org.jetbrains.annotations.NotNull;

public interface IRecipeProvider {

    /**
     * Called if the parent class defines its own recipes during data generation.
     * @param consumer The {@link RecipeOutput}
     */
    void registerRecipe(@NotNull RecipeOutput consumer);

}
