package net.superscary.nullmod.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.registries.NItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class SmeltingRecipes extends NRecipeProvider {

    private static final int DEFAULT_SMELTING_TIME = 200;

    public SmeltingRecipes (PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
    }

    @Override
    public @NotNull String getName() {
        return Null.NAME + " Smelting Recipes";
    }

    @Override
    public void buildRecipes (@NotNull RecipeOutput consumer) {
        smelting(consumer, new ItemStack(Items.IRON_INGOT), new ItemStack(NItems.STEEL_INGOT));
    }

    static void smelting(@NotNull RecipeOutput consumer, ItemStack input, ItemStack output) {
        smelting(consumer, input, output, 0.6f, DEFAULT_SMELTING_TIME * 2);
    }

    static void smelting(@NotNull RecipeOutput consumer, ItemStack input, ItemStack output, float xp, int time) {
        var hasInput = "has_" + input.getItem().getDescriptionId().toLowerCase().replace(' ', '_');
        var fileName = "smelting/" + output.getItem().getDescriptionId().toLowerCase().replace(' ', '_')
               + "_from_" + input.getItem().getDescriptionId().toLowerCase().replace(' ', '_');

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output, xp, time)
                .unlockedBy(hasInput, has(input.getItem()))
                .save(consumer, Null.getResource(fileName));
    }

}
