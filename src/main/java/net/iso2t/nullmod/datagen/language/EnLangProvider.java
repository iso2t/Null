package net.iso2t.nullmod.datagen.language;

import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.iso2t.nullmod.api.data.IDataProvider;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NBlocks;
import net.iso2t.nullmod.registries.NItems;

public class EnLangProvider extends LanguageProvider implements IDataProvider {

    public EnLangProvider(DataGenerator generator) {
        super(generator.getPackOutput(), Null.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addManualStrings();

        // ITEMS
        for (var item : NItems.getItems()) {
            add(item.asItem(), item.englishName());
        }

        // BLOCKS
        for (var block : NBlocks.getBlocks()) {
            add(block.getBlock(), block.getEnglishName());
        }
    }

    protected void addManualStrings() {
        add("itemGroup." + Null.MODID, Null.NAME);
        add("menu." + Null.MODID + ".quarry", "Dimensional Quarry");

        add("gui." + Null.MODID + ".quarry.term.header", "STATUS");
        add("gui." + Null.MODID + ".quarry.term.head", "Head: X=%s Y=%s Z=%s");
        add("gui." + Null.MODID + ".quarry.term.biome", "Biome: %s");
        add("gui." + Null.MODID + ".quarry.term.power", "Power: %s");
        add("gui." + Null.MODID + ".quarry.term.blocks_mined", "Blocks Mined: %s");

        add("gui." + Null.MODID + ".quarry.power.active", "Active");
        add("gui." + Null.MODID + ".quarry.power.idle", "Idle");
        add("gui." + Null.MODID + ".quarry.power.unformed", "Unformed");
        add("gui." + Null.MODID + ".quarry.power.redstone_off", "Disabled (Redstone)");
    }
}
