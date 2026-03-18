package net.iso2t.nullmod.api.item.base;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.iso2t.nullmod.api.data.ITagProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseItem extends Item implements ITagProvider {

    public BaseItem () {
        this(new Properties());
    }

    public BaseItem (Properties properties) {
        super(properties);
    }

    @Nullable
    public ResourceLocation getRegistryName () {
        var id = BuiltInRegistries.ITEM.getKey(this);
        return id != BuiltInRegistries.ITEM.getDefaultKey() ? id : null;
    }

    public void addToCreativeTab (CreativeModeTab.Output output) {
        output.accept(this);
    }

    @Override
    public List<TagKey<net.minecraft.world.level.block.Block>> getBlockTags(HolderLookup.@NotNull Provider provider) {
        return List.of();
    }

    @Override
    public List<TagKey<Item>> getItemTags(HolderLookup.@NotNull Provider provider) {
        return List.of();
    }

}
