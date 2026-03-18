package net.iso2t.nullmod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.iso2t.nullmod.api.item.base.BaseItem;
import org.jetbrains.annotations.Nullable;

public class BiomeMarkerItem extends BaseItem {

    public BiomeMarkerItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static ResourceLocation getBiomeId(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("Biome")) return null;
        String id = tag.getString("Biome");
        if (id.isEmpty()) return null;
        return ResourceLocation.tryParse(id);
    }

    public static void setBiomeId(ItemStack stack, @Nullable ResourceLocation biomeId) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = customData == null ? new CompoundTag() : customData.copyTag();

        if (biomeId == null) {
            tag.remove("Biome");
        } else {
            tag.putString("Biome", biomeId.toString());
        }

        if (tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
