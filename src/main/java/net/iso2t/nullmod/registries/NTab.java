package net.iso2t.nullmod.registries;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.iso2t.nullmod.api.block.base.BaseBlock;
import net.iso2t.nullmod.api.item.ItemDefinition;
import net.iso2t.nullmod.api.item.base.BaseBlockItem;
import net.iso2t.nullmod.api.item.base.BaseItem;
import net.iso2t.nullmod.core.Null;

import java.util.ArrayList;
import java.util.List;

public class NTab {

    public static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Null.getResource("main"));

    private static final Multimap<ResourceKey<CreativeModeTab>, ItemDefinition<?>> externalItemDefinitions = HashMultimap.create();
    private static final List<ItemDefinition<?>> internalItemDefinitions = new ArrayList<>();

    public static void initialize(Registry<CreativeModeTab> registry) {
        var tab = CreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + Null.MODID))
                .icon(NBlocks.QUARRY_FRAME::getStack)
                .displayItems(NTab::buildDisplayItems)
                .build();
        Registry.register(registry, MAIN, tab);
    }

    public static void initializeExternal(BuildCreativeModeTabContentsEvent event) {
        for (var entry : externalItemDefinitions.get(event.getTabKey())) {
            event.accept(entry.getStack());
        }
    }

    public static void add(ItemDefinition<?> definition) {
        internalItemDefinitions.add(definition);
    }

    public static void addExternal(ResourceKey<CreativeModeTab> tab, ItemDefinition<?> itemDefinition) {
        externalItemDefinitions.put(tab, itemDefinition);
    }

    private static void buildDisplayItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) {
        for (var itemDefinition : internalItemDefinitions) {
            var item = itemDefinition.get();
            if (item instanceof BaseBlockItem baseItem && baseItem.getBlock() instanceof BaseBlock baseBlock) {
                baseBlock.addToCreativeTab(output);
            } else if (item instanceof BaseItem baseItem) {
                baseItem.addToCreativeTab(output);
            } else {
                output.accept(itemDefinition);
            }
        }
    }

}
