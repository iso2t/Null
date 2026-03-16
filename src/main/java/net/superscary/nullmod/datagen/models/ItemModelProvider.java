package net.superscary.nullmod.datagen.models;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.superscary.nullmod.api.IDataProvider;
import net.superscary.nullmod.api.item.ItemDefinition;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.registries.NItems;

public class ItemModelProvider extends net.neoforged.neoforge.client.model.generators.ItemModelProvider implements IDataProvider {

    public ItemModelProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, Null.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (var item : NItems.getItems()) {
            handheldItem(item.get());
        }
    }

    public ItemModelBuilder handheldItem(ItemDefinition<?> item) {
        var resource = Null.getResource("item/" + item.getId().getPath());
        existingFileHelper.trackGenerated(resource, PackType.CLIENT_RESOURCES, ".png", "textures");
        return handheldItem(item.asItem());
    }

    public static boolean textureExists(ResourceLocation texture, ExistingFileHelper existingFileHelper) {
        return existingFileHelper.exists(texture, PackType.CLIENT_RESOURCES);
    }

}
