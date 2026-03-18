package net.iso2t.nullmod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.datagen.language.EnLangProvider;
import net.iso2t.nullmod.datagen.loot.LootTableProvider;
import net.iso2t.nullmod.datagen.models.BlockModelProvider;
import net.iso2t.nullmod.datagen.models.ItemModelProvider;
import net.iso2t.nullmod.datagen.recipe.CraftingRecipes;
import net.iso2t.nullmod.datagen.recipe.SmeltingRecipes;
import net.iso2t.nullmod.datagen.tags.BlockTagGenerator;
import net.iso2t.nullmod.datagen.tags.ItemTagGenerator;
import net.iso2t.nullmod.datagen.world.WorldGenProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

@EventBusSubscriber(modid = Null.MODID)
public class DataGenerators {

    @SubscribeEvent
    public static void gather (@NotNull GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var pack = generator.getVanillaPack(true);
        var existingFileHelper = event.getExistingFileHelper();
        var localization = new EnLangProvider(generator);

        // WORLD GENERATION
        pack.addProvider(output -> new WorldGenProvider(output, registries));

        // SOUNDS
        //pack.addProvider(packOutput -> new SoundProvider(packOutput, existingFileHelper));

        // LOOT TABLE
        pack.addProvider(bindRegistries(LootTableProvider::new, registries));

        // POI
        //pack.addProvider(packOutput -> new FMPoiTagGenerator(packOutput, registries, existingFileHelper));

        // TAGS
        var blockTagsProvider = pack.addProvider(pOutput -> new BlockTagGenerator(pOutput, registries, existingFileHelper));
        pack.addProvider(pOutput -> new ItemTagGenerator(pOutput, registries, blockTagsProvider.contentsGetter(), existingFileHelper));
        //pack.addProvider(packOutput -> new CoreFluidTagGenerator(packOutput, registries, existingFileHelper));

        // MODELS & STATES
        pack.addProvider(pOutput -> new BlockModelProvider(pOutput, existingFileHelper));
        pack.addProvider(pOutput -> new ItemModelProvider(pOutput, existingFileHelper));

        // RECIPES
        pack.addProvider(bindRegistries(CraftingRecipes::new, registries));
        pack.addProvider(bindRegistries(SmeltingRecipes::new, registries));
        //pack.addProvider(bindRegistries(MachineRecipes::new, registries));

        // LOCALIZATION MUST RUN LAST
        pack.addProvider(output -> localization);
    }

    @Contract(pure = true)
    private static <T extends DataProvider> DataProvider.@NotNull Factory<T> bindRegistries (BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory, CompletableFuture<HolderLookup.Provider> factories) {
        return pOutput -> factory.apply(pOutput, factories);
    }

}
