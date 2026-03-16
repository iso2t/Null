package net.superscary.nullmod.registries;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.superscary.nullmod.api.item.ItemDefinition;
import net.superscary.nullmod.item.BiomeMarkerItem;
import net.superscary.nullmod.core.Null;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class NItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(Null.MODID);
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();

    public static final ItemDefinition<BiomeMarkerItem> BIOME_MARKER = register("Biome Marker", BiomeMarkerItem::new);

    public static List<ItemDefinition<?>> getItems() {
        return Collections.unmodifiableList(ITEMS);
    }

    public static <T extends Item> ItemDefinition<T> register(String name, Function<Item.Properties, T> factory) {
        var resource = name.toLowerCase().replace(' ', '_');
        return register(name, Null.getResource(resource), factory, NTab.MAIN);
    }

    public static <T extends Item> ItemDefinition<T> register(final String name, String resource, Function<Item.Properties, T> factory) {
        return register(name, Null.getResource(resource), factory, NTab.MAIN);
    }

    public static <T extends Item> ItemDefinition<T> register(final String name, ResourceLocation resource, Function<Item.Properties, T> factory, @Nullable ResourceKey<CreativeModeTab> group) {
        Preconditions.checkArgument(resource.getNamespace().equals(Null.MODID), "Item must be in the Null mod namespace");
        var definition = new ItemDefinition<>(name, REGISTRY.registerItem(resource.getPath(), factory));

        if (Objects.equals(group, NTab.MAIN)) {
            NTab.add(definition);
        } else if (group != null) {
            NTab.addExternal(group, definition);
        }

        ITEMS.add(definition);
        return definition;
    }

}
