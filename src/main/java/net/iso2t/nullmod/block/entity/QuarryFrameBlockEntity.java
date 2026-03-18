package net.iso2t.nullmod.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.iso2t.nullmod.api.energy.NullEnergyStorage;
import net.iso2t.nullmod.api.item.ExportOnlyItemStackHandler;
import net.iso2t.nullmod.block.SatelliteBlock;
import net.iso2t.nullmod.registries.NBlockEntities;
import net.iso2t.nullmod.registries.NBlocks;
import net.iso2t.nullmod.world.quarry.QuarryManager;
import net.iso2t.nullmod.world.quarry.QuarryMiningAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class QuarryFrameBlockEntity extends BlockEntity {

    private final QuarryManager quarryManager = new QuarryManager();

    @Getter
    private boolean formed;

    private int revalidateCooldown;

    @Getter
    private boolean active;

    private long quarryInstanceSalt;

    private final NullEnergyStorage energyStorage = new NullEnergyStorage(20_000_000, 200_000, 200_000, 0, this::setChanged);

    private final ExportOnlyItemStackHandler exportItems = new ExportOnlyItemStackHandler(9, this::setChanged);

    @Getter
    private final FluidTank fluidTank = new FluidTank(10_000) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            syncToClient();
        }
    };

    private final ItemStackHandler quarrySlots = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            setChanged();
        }
    };

    public String getCurrentBiome() {
        var pos = new BlockPos(quarryManager.getMineX(), quarryManager.getMineY(), quarryManager.getMineZ());
        return Objects.requireNonNull(getLevel().getBiome(pos).unwrapKey().map(ResourceKey::location).orElse(null)).toString();
    }

    private ItemStack buildMiningTool() {
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
        applyBookEnchantments(tool, quarrySlots.getStackInSlot(1));
        applyBookEnchantments(tool, quarrySlots.getStackInSlot(2));
        return tool;
    }

    private static void applyBookEnchantments(ItemStack tool, ItemStack book) {
        if (!book.is(Items.ENCHANTED_BOOK)) return;
        ItemEnchantments enchants = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchants.entrySet()) {
            tool.enchant(entry.getKey(), entry.getIntValue());
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuarryFrameBlockEntity blockEntity) {
        if (blockEntity.revalidateCooldown-- <= 0) {
            blockEntity.revalidateCooldown = 20;
            blockEntity.revalidateMultiblock();
        }

        if (!(level instanceof ServerLevel serverLevel)) return;

        if (serverLevel.hasNeighborSignal(pos)) {
            blockEntity.setActive(false, serverLevel);
            return;
        }

        if (!blockEntity.formed) {
            blockEntity.setActive(false, serverLevel);
            return;
        }

        blockEntity.setActive(true, serverLevel);
        blockEntity.quarryManager.tick(serverLevel, blockEntity.miningAccess);
    }

    private void setActive(boolean active, ServerLevel serverLevel) {
        if (this.active == active) return;
        this.active = active;

        ensureSaltInitialized();
        quarryManager.ensureAssignedChunk(serverLevel, serverLevel.getSeed(), worldPosition, quarryInstanceSalt);
        quarryManager.setActive(active, serverLevel);
        setChanged();
    }

    private void ensureSaltInitialized() {
        if (quarryInstanceSalt != 0L) return;
        quarryInstanceSalt = ThreadLocalRandom.current().nextLong();
    }

    private void syncToClient() {
        if (level == null || level.isClientSide) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private ItemStack insertIntoExport(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < exportItems.getSlots(); slot++) {
            if (remaining.isEmpty()) break;
            remaining = exportItems.forceInsertItem(slot, remaining, false);
        }
        return remaining;
    }

    private final QuarryMiningAccess miningAccess = new QuarryMiningAccess() {
        @Override
        public BlockPos getQuarryControllerPos() {
            return worldPosition;
        }

        @Override
        public long getQuarryInstanceSalt() {
            QuarryFrameBlockEntity.this.ensureSaltInitialized();
            return quarryInstanceSalt;
        }

        @Override
        public IEnergyStorage getEnergyStorage() {
            return energyStorage;
        }

        @Override
        public FluidTank getFluidTank() {
            return fluidTank;
        }

        @Override
        public ItemStack buildMiningTool() {
            return QuarryFrameBlockEntity.this.buildMiningTool();
        }

        @Override
        public ItemStack insertIntoExport(ItemStack stack) {
            return QuarryFrameBlockEntity.this.insertIntoExport(stack);
        }

        @Override
        public void markDirtyAndSync() {
            setChanged();
            syncToClient();
        }
    };

    public int getMineX() {
        return quarryManager.getMineX();
    }

    public int getMineY() {
        return quarryManager.getMineY();
    }

    public int getMineZ() {
        return quarryManager.getMineZ();
    }

    public BigInteger getBlocksMined() {
        return quarryManager.getBlocksMined();
    }

    public String getStatus() {
        return quarryManager.getStatus();
    }

    public QuarryFrameBlockEntity(BlockPos pos, BlockState state) {
        super(NBlockEntities.QUARRY_FRAME_ENTITY.get(), pos, state);
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    public IItemHandler getItemHandler() {
        return exportItems;
    }

    public IItemHandler getQuarrySlots() {
        return quarrySlots;
    }

    public void revalidateMultiblock() {
        if (level == null || level.isClientSide) return;

        boolean valid = true;
        for (Direction dir : Direction.values()) {
            BlockPos satellitePos = worldPosition.relative(dir);
            BlockState state = level.getBlockState(satellitePos);

            if (!state.is(NBlocks.QUARRY_SATELLITE.getBlock())) {
                valid = false;
                break;
            }
            if (!state.hasProperty(SatelliteBlock.FACING) || state.getValue(SatelliteBlock.FACING) != dir) {
                valid = false;
                break;
            }
            if (!(level.getBlockEntity(satellitePos) instanceof SatelliteBlockEntity)) {
                valid = false;
                break;
            }
        }

        setFormed(valid);
        for (Direction dir : Direction.values()) {
            BlockPos satellitePos = worldPosition.relative(dir);
            if (level.getBlockEntity(satellitePos) instanceof SatelliteBlockEntity satellite) {
                satellite.setControllerPos(valid ? worldPosition : null);
            }
        }
    }

    private void setFormed(boolean formed) {
        if (this.formed != formed) {
            this.formed = formed;
            setChanged();
            syncToClient();

            if (level != null && !level.isClientSide) {
                for (Direction dir : Direction.values()) {
                    BlockPos satellitePos = worldPosition.relative(dir);
                    if (level.getBlockEntity(satellitePos) instanceof SatelliteBlockEntity satellite) {
                        satellite.refreshConnections();
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ensureSaltInitialized();
        CompoundTag energyTag = new CompoundTag();
        energyStorage.save(energyTag);
        tag.put("Energy", energyTag);
        CompoundTag itemsTag = new CompoundTag();
        exportItems.save(itemsTag, registries);
        tag.put("Items", itemsTag);

        tag.put("Tank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.put("QuarrySlots", quarrySlots.serializeNBT(registries));
        tag.putBoolean("Formed", formed);

        tag.putLong("QuarrySalt", quarryInstanceSalt);

        quarryManager.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        quarryInstanceSalt = tag.contains("QuarrySalt") ? tag.getLong("QuarrySalt") : 0L;
        ensureSaltInitialized();
        if (tag.contains("Energy")) {
            energyStorage.load(tag.getCompound("Energy"));
        }
        if (tag.contains("Items")) {
            exportItems.load(tag.getCompound("Items"), registries);
        }

        if (tag.contains("Tank")) {
            fluidTank.readFromNBT(registries, tag.getCompound("Tank"));
        }
        if (tag.contains("QuarrySlots")) {
            quarrySlots.deserializeNBT(registries, tag.getCompound("QuarrySlots"));
        }
        formed = tag.getBoolean("Formed");

        quarryManager.loadAdditional(tag, registries);
        active = quarryManager.isActive();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        loadAdditional(tag, registries);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

}
