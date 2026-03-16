package net.superscary.nullmod.registries;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.superscary.nullmod.api.block.BlockDefinition;
import net.superscary.nullmod.api.block.base.BaseBlock;
import net.superscary.nullmod.api.item.ItemDefinition;
import net.superscary.nullmod.api.item.base.BaseBlockItem;
import net.superscary.nullmod.block.QuarryFrameBlock;
import net.superscary.nullmod.block.SatelliteBlock;
import net.superscary.nullmod.core.Null;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class NBlocks {

    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(Null.MODID);
    private static final List<BlockDefinition<?>> BLOCKS = new ArrayList<>();

    public static final BlockDefinition<SatelliteBlock> QUARRY_SATELLITE = register("Quarry Satellite", SatelliteBlock::new);
    public static final BlockDefinition<QuarryFrameBlock> QUARRY_FRAME   = register("Quarry Frame", QuarryFrameBlock::new);

    public static List<BlockDefinition<?>> getBlocks () {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static <T extends Block> BlockDefinition<T> register(final String name, final Supplier<T> supplier) {
        String resourceFriendly = name.toLowerCase().replace(' ', '_');
        return register(name, Null.getResource(resourceFriendly), supplier, null, true);
    }

    public static <T extends Block> BlockDefinition<T> register(final String name, String resourceName, final Supplier<T> supplier) {
        return register(name, Null.getResource(resourceName), supplier, null, true);
    }

    public static <T extends Block> BlockDefinition<T> register(final String name, ResourceLocation id, final Supplier<T> supplier, @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory, boolean addToTab) {
        return register(name, id, supplier, itemFactory, addToTab, NTab.MAIN);
    }

    public static <T extends Block> BlockDefinition<T> register(final String name, ResourceLocation id, final Supplier<T> supplier, @Nullable BiFunction<Block, Item.Properties, BlockItem> itemFactory, boolean addToTab, @Nullable ResourceKey<CreativeModeTab> group) {
        var deferredBlock = REGISTRY.register(id.getPath(), supplier);
        var deferredItem = NItems.REGISTRY.register(id.getPath(), () -> {
            var block = deferredBlock.get();
            var itemProperties = new Item.Properties();
            if (itemFactory != null) {
                var item = itemFactory.apply(block, itemProperties);
                if (item == null) {
                    throw new IllegalArgumentException("BlockItem factory for " + id + " returned null.");
                }
                return item;
            } else if (block instanceof BaseBlock) {
                return new BaseBlockItem(block, itemProperties);
            } else {
                return new BlockItem(block, itemProperties);
            }
        });
        var itemDef = new ItemDefinition<>(name, deferredItem);
        if (addToTab) {
            if (Objects.equals(group, NTab.MAIN)) {
                NTab.add(itemDef);
            } else if (group != null) {
                NTab.addExternal(group, itemDef);
            }
        }
        BlockDefinition<T> definition = new BlockDefinition<>(name, deferredBlock, itemDef);
        BLOCKS.add(definition);
        return definition;
    }

}
