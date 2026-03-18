package net.iso2t.nullmod.api.block;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.iso2t.nullmod.api.data.IRecipeProvider;
import net.iso2t.nullmod.api.data.ITagProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.datagen.recipe.CraftingRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NSlabBlock extends SlabBlock implements IRecipeProvider, ITagProvider {

    @Getter
    private final BlockDefinition<?> parent;

    public NSlabBlock(Properties properties, BlockDefinition<?> parent) {
        super(properties);
        this.parent = parent;
    }

    @Override
    public void registerRecipe(@NotNull RecipeOutput consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, this, 6)
                .pattern("###")
                .define('#', parent.getBlock())
                .unlockedBy("has_" + parent.getId().getPath().toLowerCase().replace(' ', '_'), CraftingRecipes.inventoryHas(parent))
                .save(consumer, Null.getResource("crafting/" + this.getDescriptionId() + "_from_" + parent.getId().getPath().toLowerCase().replace(' ', '_')));
    }

    @Override
    public List<TagKey<Block>> getBlockTags(HolderLookup.@NotNull Provider provider) {
        return List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.SLABS);
    }

    @Override
    public List<TagKey<Item>> getItemTags(HolderLookup.@NotNull Provider provider) {
        return List.of();
    }

}
