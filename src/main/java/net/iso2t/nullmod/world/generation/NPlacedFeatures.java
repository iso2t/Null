package net.iso2t.nullmod.world.generation;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.iso2t.nullmod.core.Null;

import java.util.List;

public class NPlacedFeatures {

    public static final ResourceKey<PlacedFeature> MARBLE_DEPOSIT_KEY = registerKey("marble_deposit_key");
    public static final ResourceKey<PlacedFeature> BASALT_DEPOSIT_KEY = registerKey("basalt_deposit_key");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, MARBLE_DEPOSIT_KEY, configuredFeatures.getOrThrow(NConfiguredFeatures.OVERWORLD_MARBLE_DEPOSIT_KEY),
                OrePlacement.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.absolute(128))));

        register(context, BASALT_DEPOSIT_KEY, configuredFeatures.getOrThrow(NConfiguredFeatures.OVERWORLD_BASALT_DEPOSIT_KEY),
                OrePlacement.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.BOTTOM, VerticalAnchor.absolute(64))));
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Null.getResource(name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

}
