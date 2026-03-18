package net.iso2t.nullmod.api.block;

import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.iso2t.nullmod.api.block.base.BaseBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DecorationBlock extends BaseBlock {

    public DecorationBlock(Properties properties) {
        super(properties);
    }

    public DecorationBlock() {
        this(Properties.ofFullCopy(Blocks.STONE));
    }

    @Override
    public List<TagKey<Block>> getBlockTags(HolderLookup.@NotNull Provider provider) {
        return List.of(BlockTags.NEEDS_STONE_TOOL);
    }

}
