package net.superscary.nullmod.datagen.models;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.superscary.nullmod.api.block.BlockDefinition;
import net.superscary.nullmod.block.SatelliteBlock;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.registries.NBlocks;

import java.util.List;

public class BlockModelProvider extends BlockStateProvider {

    public BlockModelProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Null.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var block : NBlocks.getBlocks()) {
            if (block.getBlock() instanceof SatelliteBlock) {
                satellite(block);
            } else {
                blockWithItem(block);
            }
        }
    }

    private void satellite(BlockDefinition<?> block) {
        var top = modLoc("block/quarry_satellite_top");
        var bottom = modLoc("block/quarry_satellite_bottom");
        var side = modLoc("block/quarry_satellite_side");

        var model = models().cube("block/" + block.getId().getPath(), bottom, top, side, side, side, side)
                .texture("particle", side);

        directionalBlock(block.getBlock(), model);
        simpleBlockItem(block.getBlock(), model);
    }

    private void blockWithItem(BlockDefinition<?> block) {
        err(List.of(block.getId()));
        simpleBlockWithItem(block.getBlock(), cubeAll(block.getBlock()));
    }

    private void err(List<ResourceLocation> list) {
        for (var res : list) {
            existingFileHelper.trackGenerated(res, PackType.CLIENT_RESOURCES, ".png", "textures");
        }
    }

}
