package net.superscary.nullmod.datagen.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.registries.NBlocks;
import net.superscary.nullmod.registries.NItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CraftingRecipes extends NRecipeProvider {

    public CraftingRecipes(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
    }

    @Override
    public @NotNull String getName() {
        return Null.NAME + " Crafting Recipes";
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NBlocks.MACHINE_FRAME, 1)
                .pattern("SGS")
                .pattern("GSG")
                .pattern("SGS")
                .define('S', NItems.STEEL_INGOT)
                .define('G', Items.BLUE_STAINED_GLASS_PANE)
                .unlockedBy("has_steel_ingot", has(NItems.STEEL_INGOT))
                .unlockedBy("has_glass_pane", has(Items.BLUE_STAINED_GLASS_PANE))
                .save(consumer, Null.getResource("crafting/machine_frame"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NItems.GUIDE, 1)
                .pattern("R")
                .pattern("S")
                .define('R', Items.REDSTONE)
                .define('S', NItems.STEEL_INGOT)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .unlockedBy("has_steel_ingot", has(NItems.STEEL_INGOT))
                .save(consumer, Null.getResource("crafting/guide"));
    }
}
