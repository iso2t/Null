package net.iso2t.nullmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.iso2t.nullmod.registries.NBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
        boolean changed = !Objects.equals(controllerPos, this.controllerPos);
        this.controllerPos = controllerPos;
        if (changed) {
            setChanged();
            refreshConnections();
        }
    }

    public void refreshConnections() {
        if (level == null || level.isClientSide) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
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
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ControllerPos")) {
            controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        } else {
            controllerPos = null;
        }
    }

}
