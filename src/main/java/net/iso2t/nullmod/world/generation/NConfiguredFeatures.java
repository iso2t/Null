package net.iso2t.nullmod.world.generation;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NBlocks;

import java.util.List;

public class NConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_MARBLE_DEPOSIT_KEY = registerKey("overworld_marble_deposit");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OVERWORLD_BASALT_DEPOSIT_KEY = registerKey("overworld_basalt_deposit");

    public static void bootstrap (BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        var basaltList = List.of(OreConfiguration.target(stoneReplaceables, NBlocks.BASALT.getBlock().defaultBlockState()), OreConfiguration.target(deepslateReplaceables, NBlocks.BASALT.getBlock().defaultBlockState()));

        register(context, OVERWORLD_MARBLE_DEPOSIT_KEY, Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(stoneReplaceables, NBlocks.MARBLE.getBlock().defaultBlockState())), 64));
        register(context, OVERWORLD_BASALT_DEPOSIT_KEY, Feature.ORE, new OreConfiguration(basaltList, 32));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey (String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Null.getResource(name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register (BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

}
