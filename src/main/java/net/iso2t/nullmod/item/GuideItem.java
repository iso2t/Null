package net.iso2t.nullmod.item;

import guideme.GuidesCommon;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.iso2t.nullmod.api.data.IRecipeProvider;
import net.iso2t.nullmod.api.item.base.BaseItem;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.datagen.recipe.CraftingRecipes;
import net.iso2t.nullmod.registries.NItems;
import org.jetbrains.annotations.NotNull;

public class GuideItem extends BaseItem implements IRecipeProvider {

    public GuideItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        if (level.isClientSide) {
            GuidesCommon.openGuide(player, Null.getResource("guide"));
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, player.getItemInHand(usedHand));
    }

    @Override
    public void registerRecipe(@NotNull RecipeOutput consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, this, 1)
                .pattern("R")
                .pattern("S")
                .define('R', Items.REDSTONE)
                .define('S', NItems.STEEL_INGOT)
                .unlockedBy("has_redstone", CraftingRecipes.inventoryHas(Items.REDSTONE))
                .unlockedBy("has_steel_ingot", CraftingRecipes.inventoryHas(NItems.STEEL_INGOT))
                .save(consumer, Null.getResource("crafting/guide"));
    }
}
