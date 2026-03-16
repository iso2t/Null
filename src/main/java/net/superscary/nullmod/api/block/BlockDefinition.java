package net.superscary.nullmod.api.block;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.superscary.nullmod.api.item.ItemDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockDefinition<T extends Block> implements ItemLike, java.util.function.Supplier<T> {

    @Getter
    private final String englishName;
    private final ItemDefinition<BlockItem> item;
    private final DeferredBlock<T> block;

    public BlockDefinition(String englishName, DeferredBlock<T> block, ItemDefinition<BlockItem> item) {
        this.englishName = englishName;
        this.item = Objects.requireNonNull(item, "item");
        this.block = Objects.requireNonNull(block, "block");
    }

    public String getRegistryFriendlyName() {
        return englishName.toLowerCase().replace(' ', '_');
    }

    public ResourceLocation getId() {
        return block.getId();
    }

    @Override
    public T get() {
        return getBlock();
    }

    public final T getBlock() {
        return this.block.get();
    }

    public ItemStack getStack() {
        return item.getStack();
    }

    public ItemStack getStack(int stackSize) {
        return item.getStack(stackSize);
    }

    public ItemDefinition<BlockItem> item() {
        return item;
    }

    @Override
    public @NotNull Item asItem() {
        return item.asItem();
    }

}
