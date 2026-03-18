package net.iso2t.nullmod.datagen.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.iso2t.nullmod.api.data.IDataProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.world.generation.NBiomeModifiers;
import net.iso2t.nullmod.world.generation.NConfiguredFeatures;
import net.iso2t.nullmod.world.generation.NPlacedFeatures;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WorldGenProvider extends DatapackBuiltinEntriesProvider implements IDataProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, NConfiguredFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, NPlacedFeatures::bootstrap)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, NBiomeModifiers::bootstrap);

    public WorldGenProvider (PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(Null.MODID));
    }

    @Override
    public @NotNull String getName () {
        return Null.NAME + " World Gen";
    }

}
