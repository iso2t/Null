package net.iso2t.nullmod.datagen.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.iso2t.nullmod.api.data.IRecipeProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NBlocks;
import net.iso2t.nullmod.registries.NItems;
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

        for (var item : NItems.getItems()) {
            if (item.get() instanceof IRecipeProvider provider) {
                provider.registerRecipe(consumer);
            }
        }

        for (var block : NBlocks.getBlocks()) {
            if (block.get() instanceof IRecipeProvider provider) {
                provider.registerRecipe(consumer);
            }
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, NBlocks.MACHINE_FRAME, 1)
                .pattern("SGS")
                .pattern("GSG")
                .pattern("SGS")
                .define('S', NItems.STEEL_INGOT)
                .define('G', Items.BLUE_STAINED_GLASS_PANE)
                .unlockedBy("has_steel_ingot", has(NItems.STEEL_INGOT))
                .unlockedBy("has_glass_pane", has(Items.BLUE_STAINED_GLASS_PANE))
                .save(consumer, Null.getResource("crafting/machine_frame"));

    }

    public static @NotNull Criterion<InventoryChangeTrigger.TriggerInstance> inventoryHas(ItemLike itemLike) {
        return has(itemLike);
    }
}
