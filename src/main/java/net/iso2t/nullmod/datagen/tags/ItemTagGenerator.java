package net.iso2t.nullmod.datagen.tags;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.iso2t.nullmod.api.data.IDataProvider;
import net.iso2t.nullmod.api.data.ITagProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends ItemTagsProvider implements IDataProvider {

    public ItemTagGenerator(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> future, CompletableFuture<TagLookup<Block>> completableFuture, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, future, completableFuture, Null.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        for (var itemDef : NItems.getItems()) {
            var item = itemDef.get();
            if (item instanceof ITagProvider tagProvider) {
                for (var tag : tagProvider.getItemTags(provider)) {
                    this.tag(tag).add(item);
                }
            }

            if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ITagProvider tagProvider) {
                for (var tag : tagProvider.getItemTags(provider)) {
                    this.tag(tag).add(item);
                }
            }
        }
    }

}
