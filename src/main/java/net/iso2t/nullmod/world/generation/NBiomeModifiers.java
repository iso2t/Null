package net.iso2t.nullmod.world.generation;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.iso2t.nullmod.core.Null;

public class NBiomeModifiers {

    public static final ResourceKey<BiomeModifier> MARBLE_DEPOSIT = register("marble_deposit");
    public static final ResourceKey<BiomeModifier> BASALT_DEPOSIT = register("basalt_deposit");

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var placedBiomes = context.lookup(Registries.BIOME);

        context.register(MARBLE_DEPOSIT, new BiomeModifiers.AddFeaturesBiomeModifier(
                placedBiomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(NPlacedFeatures.MARBLE_DEPOSIT_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(BASALT_DEPOSIT, new BiomeModifiers.AddFeaturesBiomeModifier(
                placedBiomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(NPlacedFeatures.BASALT_DEPOSIT_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> register(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Null.getResource(name));
    }

}
