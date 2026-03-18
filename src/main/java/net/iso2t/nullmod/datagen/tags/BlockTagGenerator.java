package net.iso2t.nullmod.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.iso2t.nullmod.api.data.IDataProvider;
import net.iso2t.nullmod.api.data.ITagProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends BlockTagsProvider implements IDataProvider {

    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Null.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        for (var block : NBlocks.getBlocks()) {
            this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.getBlock());
            if (block.getBlock() instanceof ITagProvider tagProvider) {
                for (var tag : tagProvider.getBlockTags(provider)) {
                    this.tag(tag).add(block.getBlock());
                }
            } else {
                this.tag(BlockTags.NEEDS_DIAMOND_TOOL).add(block.getBlock());
            }
        }
    }
}
