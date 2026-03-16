package net.superscary.nullmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.superscary.nullmod.registries.NBlockEntities;
import org.jetbrains.annotations.Nullable;

public class SatelliteBlockEntity extends BlockEntity {

    @Nullable
    private BlockPos controllerPos;

    public SatelliteBlockEntity(BlockPos pos, BlockState state) {
        super(NBlockEntities.SATELLITE_ENTITY.get(), pos, state);
    }

    @Nullable
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(@Nullable BlockPos controllerPos) {
        if (controllerPos == null ? this.controllerPos != null : !controllerPos.equals(this.controllerPos)) {
            this.controllerPos = controllerPos;
            setChanged();
        }
    }

    @Nullable
    public QuarryFrameBlockEntity getController() {
        if (level == null || controllerPos == null) return null;
        if (level.getBlockEntity(controllerPos) instanceof QuarryFrameBlockEntity controller) {
            return controller;
        }
        return null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        } else {
            controllerPos = null;
        }
    }

}
