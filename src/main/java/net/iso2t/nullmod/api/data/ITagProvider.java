package net.iso2t.nullmod.api.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ITagProvider {

    /**
     * Defines block tags for data generation
     * @param provider The lookup provider
     * @return a list of tags
     */
    List<TagKey<Block>> getBlockTags(HolderLookup.@NotNull Provider provider);

    /**
     * Defines item tags for data generation
     * @param provider The lookup provider
     * @return a list of tags
     */
    List<TagKey<Item>> getItemTags(HolderLookup.@NotNull Provider provider);

}
