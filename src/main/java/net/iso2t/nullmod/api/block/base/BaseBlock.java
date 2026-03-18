package net.iso2t.nullmod.api.block.base;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.iso2t.nullmod.api.data.ITagProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BaseBlock extends Block implements ITagProvider {

    public BaseBlock (Properties properties) {
        super(properties);
    }

    public BaseBlock() {
        this(Properties.ofFullCopy(Blocks.IRON_BLOCK));
    }

    public void addToCreativeTab (CreativeModeTab.Output output) {
        output.accept(this);
    }

    @Override
    public @NotNull String toString () {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getPath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @Nullable
    public ResourceLocation getRegistryName () {
        var id = BuiltInRegistries.BLOCK.getKey(this);
        return id != BuiltInRegistries.BLOCK.getDefaultKey() ? id : null;
    }

    @Override
    public List<TagKey<Block>> getBlockTags(HolderLookup.@NotNull Provider provider) {
        return List.of(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    @Override
    public List<TagKey<Item>> getItemTags(HolderLookup.@NotNull Provider provider) {
        return List.of();
    }

}
