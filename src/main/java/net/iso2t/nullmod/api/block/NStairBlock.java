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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.iso2t.nullmod.api.data.IRecipeProvider;
import net.iso2t.nullmod.api.data.ITagProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.datagen.recipe.CraftingRecipes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NStairBlock extends StairBlock implements IRecipeProvider, ITagProvider {

    @Getter
    private final BlockDefinition<?> parent;

    public NStairBlock(BlockState baseState, BlockBehaviour.Properties properties, BlockDefinition<?> parent) {
        super(baseState, properties);
        this.parent = parent;
    }

    @Override
    public void registerRecipe(@NotNull RecipeOutput consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, this, 4)
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', parent.getBlock())
                .unlockedBy("has_" + parent.getId().getPath().toLowerCase().replace(' ', '_'), CraftingRecipes.inventoryHas(parent))
                .save(consumer, Null.getResource("crafting/" + this.getDescriptionId() + "_from_" + parent.getId().getPath().toLowerCase().replace(' ', '_')));
    }

    @Override
    public List<TagKey<Block>> getBlockTags(HolderLookup.@NotNull Provider provider) {
        return List.of(BlockTags.STAIRS, BlockTags.NEEDS_STONE_TOOL);
    }

    @Override
    public List<TagKey<Item>> getItemTags(HolderLookup.@NotNull Provider provider) {
        return List.of();
    }

}
