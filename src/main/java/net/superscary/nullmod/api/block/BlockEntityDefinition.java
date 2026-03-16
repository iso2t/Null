package net.superscary.nullmod.api.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Objects;

public record BlockEntityDefinition<T extends BlockEntity>(String englishName, DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> type) {

    public BlockEntityDefinition {
        Objects.requireNonNull(englishName, "englishName");
        Objects.requireNonNull(type, "type");
    }

    public ResourceLocation getId() {
        return type.getId();
    }

    public BlockEntityType<T> get() {
        return type.get();
    }

    public String getRegistryFriendlyName() {
        return englishName.toLowerCase().replace(' ', '_');
    }

}
