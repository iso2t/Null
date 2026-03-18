package net.iso2t.nullmod.api.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record ItemDefinition<T extends Item>(String englishName, DeferredItem<T> item) implements ItemLike, Supplier<T> {

    public ResourceLocation getId() {
        return this.item.getId();
    }

    public ItemStack getStack() {
        return getStack(1);
    }

    public ItemStack getStack(int count) {
        return new ItemStack(this, count);
    }

    public Holder<Item> getHolder() {
        return item;
    }

    public String getRegistryFriendlyName() {
        return englishName.toLowerCase().replace(' ', '_');
    }

    @Override
    public T get() {
        return item.get();
    }

    @Override
    public @NotNull T asItem() {
        return get();
    }

}
