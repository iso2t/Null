package net.superscary.nullmod.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.superscary.nullmod.api.IDataProvider;

import java.util.concurrent.CompletableFuture;

public abstract class NRecipeProvider extends RecipeProvider implements IDataProvider {

    public NRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
    }

}
