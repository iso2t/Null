package net.iso2t.nullmod.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.iso2t.nullmod.api.block.BlockEntityDefinition;
import net.iso2t.nullmod.block.entity.QuarryFrameBlockEntity;
import net.iso2t.nullmod.block.entity.SatelliteBlockEntity;
import net.iso2t.nullmod.core.Null;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class NBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Null.MODID);
    private static final List<BlockEntityDefinition<?>> BLOCK_ENTITIES = new ArrayList<>();

    public static final BlockEntityDefinition<QuarryFrameBlockEntity> QUARRY_FRAME_ENTITY = register("Quarry Frame", QuarryFrameBlockEntity::new, NBlocks.QUARRY_FRAME);
    public static final BlockEntityDefinition<SatelliteBlockEntity> SATELLITE_ENTITY = register("Satellite", SatelliteBlockEntity::new, NBlocks.QUARRY_SATELLITE);

    public static List<BlockEntityDefinition<?>> getBlockEntities() {
        return Collections.unmodifiableList(BLOCK_ENTITIES);
    }

    @SafeVarargs
    public static <T extends BlockEntity> BlockEntityDefinition<T> register(final String name, final BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... blocks) {
        String resourceFriendly = name.toLowerCase().replace(' ', '_');
        return register(name, resourceFriendly, supplier, blocks);
    }

    @SafeVarargs
    public static <T extends BlockEntity> BlockEntityDefinition<T> register(final String name, String resourceFriendly, final BlockEntityType.BlockEntitySupplier<T> supplier, Supplier<? extends Block>... blocks) {
        var type = REGISTRY.register(resourceFriendly, () -> BlockEntityType.Builder.of(supplier, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)).build(null));
        BlockEntityDefinition<T> definition = new BlockEntityDefinition<>(name, type);
        BLOCK_ENTITIES.add(definition);
        return definition;
    }

}
