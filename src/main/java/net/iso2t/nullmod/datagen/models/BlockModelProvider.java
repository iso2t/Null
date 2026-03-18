package net.iso2t.nullmod.datagen.models;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.iso2t.nullmod.api.block.BlockDefinition;
import net.iso2t.nullmod.api.block.NSlabBlock;
import net.iso2t.nullmod.api.block.NStairBlock;
import net.iso2t.nullmod.api.block.NWallBlock;
import net.iso2t.nullmod.block.SatelliteBlock;
import net.iso2t.nullmod.core.Null;
import net.iso2t.nullmod.registries.NBlocks;

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
            } else if (block.getBlock() instanceof NStairBlock stair) {
                stairsBlock(stair, blockTexture(stair.getParent().getBlock()));
                blockItem(block);
            } else if (block.getBlock() instanceof NSlabBlock slab) {
                slabBlock(slab, blockTexture(slab.getParent().getBlock()), blockTexture(slab.getParent().getBlock()));
                blockItem(block);
            } else if (block.getBlock() instanceof NWallBlock wall) {
                wallBlock(wall, blockTexture(wall.getParent().getBlock()));
                itemModels().wallInventory(block.getId().getPath(), blockTexture(wall.getParent().getBlock()));
            } else {
                blockWithItem(block);
            }
        }
    }

    private void blockItem (BlockDefinition<?> blockRegistryObject) {
        simpleBlockItem(blockRegistryObject.getBlock(), new ModelFile.UncheckedModelFile(Null.MODID + ":block/" + blockRegistryObject.getId().getPath()));
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
