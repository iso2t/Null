package net.superscary.nullmod.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.superscary.nullmod.api.energy.NullEnergyStorage;
import net.superscary.nullmod.api.item.ExportOnlyItemStackHandler;
import net.superscary.nullmod.block.SatelliteBlock;
import net.superscary.nullmod.core.Null;
import net.superscary.nullmod.item.BiomeMarkerItem;
import net.superscary.nullmod.registries.NBlockEntities;
import net.superscary.nullmod.registries.NBlocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Objects;

public class QuarryFrameBlockEntity extends BlockEntity {

    private static final Block REPLACE_WITH = Blocks.DIRT;

    private int forcedChunkX = Integer.MIN_VALUE;
    private int forcedChunkZ = Integer.MIN_VALUE;

    @Getter
    private boolean formed;

    private int revalidateCooldown;

    @Getter
    private boolean active;

    private int quarryChunkX;
    private int quarryChunkZ;

    @Getter
    private int mineX;

    @Getter
    private int mineY;

    @Getter
    private int mineZ;

    private String selectedBiomeId;

    @Getter
    private BigInteger blocksMined = BigInteger.ZERO;

    private static final int DEFAULT_RF_PER_BLOCK = 200_000;

    private static final ResourceKey<Level> QUARRY_LEVEL = ResourceKey.create(Registries.DIMENSION, Null.getResource("quarry"));

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
        assert getLevel() != null;
        return Objects.requireNonNull(getLevel().getBiome(worldPosition).unwrapKey().map(ResourceKey::location).orElse(null)).toString();
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

        if (!blockEntity.active) {
            blockEntity.setActive(true, serverLevel);
        }

        ServerLevel quarryLevel = serverLevel.getServer().getLevel(QUARRY_LEVEL);
        if (quarryLevel == null) return;

        blockEntity.forceChunkLoaded(quarryLevel, true);
        blockEntity.tryMineOneBlock(serverLevel, quarryLevel);
    }

    private void setActive(boolean active, ServerLevel serverLevel) {
        if (this.active == active) return;
        this.active = active;

        if (active) {
            ensureAssignedChunk();
            initializeCursorIfNeeded(serverLevel);
            updateSelectedBiome(serverLevel);
        } else {
            ServerLevel quarryLevel = serverLevel.getServer().getLevel(QUARRY_LEVEL);
            if (quarryLevel != null) {
                forceChunkLoaded(quarryLevel, false);
            }
        }
        setChanged();
    }

    private void ensureAssignedChunk() {
        if (quarryChunkX != 0 || quarryChunkZ != 0) return;

        long worldSeed = 0L;
        if (level instanceof ServerLevel serverLevel) {
            worldSeed = serverLevel.getSeed();
        }

        long mixed = mix64(worldPosition.asLong() ^ (worldSeed * 0x9E3779B97F4A7C15L));
        int baseX = (int) mixed;
        int baseZ = (int) (mixed >>> 32);

        quarryChunkX = (baseX & 0x7FFF) * 2;
        quarryChunkZ = (baseZ & 0x7FFF) * 2;

        if (quarryChunkX == 0 && quarryChunkZ == 0) {
            quarryChunkX = 2;
        }
    }

    private void initializeCursorIfNeeded(ServerLevel serverLevel) {
        if (mineY != 0) return;
        ensureAssignedChunk();
        int startX = (quarryChunkX << 4);
        int startZ = (quarryChunkZ << 4);

        long mixed = mix64(worldPosition.asLong() ^ (serverLevel.getSeed() * 0xD6E8FEB86659FD93L));
        int localX = (int) (mixed & 15L);
        int localZ = (int) ((mixed >>> 4) & 15L);
        mineX = startX + localX;
        mineZ = startZ + localZ;
        ServerLevel quarryLevel = serverLevel.getServer().getLevel(QUARRY_LEVEL);
        if (quarryLevel == null) {
            mineY = serverLevel.getMaxBuildHeight() - 1;
            return;
        }

        resetYForColumn(quarryLevel);
    }

    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private void updateSelectedBiome(ServerLevel overworld) {
        // Slot 0 is biome marker.
        ItemStack marker = quarrySlots.getStackInSlot(0);
        ResourceLocation biomeId = BiomeMarkerItem.getBiomeId(marker);
        if (biomeId == null) {
            var biome = overworld.getBiome(this.worldPosition);
            biomeId = biome.unwrapKey().map(ResourceKey::location).orElse(null);
        }
        selectedBiomeId = biomeId == null ? null : biomeId.toString();
    }

    private void forceChunkLoaded(ServerLevel quarryLevel, boolean forced) {
        ensureAssignedChunk();

        if (!forced) {
            if (forcedChunkX != Integer.MIN_VALUE && forcedChunkZ != Integer.MIN_VALUE) {
                quarryLevel.setChunkForced(forcedChunkX, forcedChunkZ, false);
                forcedChunkX = Integer.MIN_VALUE;
                forcedChunkZ = Integer.MIN_VALUE;
            }
            return;
        }

        if (forcedChunkX != Integer.MIN_VALUE && forcedChunkZ != Integer.MIN_VALUE
                && (forcedChunkX != quarryChunkX || forcedChunkZ != quarryChunkZ)) {
            quarryLevel.setChunkForced(forcedChunkX, forcedChunkZ, false);
        }

        quarryLevel.setChunkForced(quarryChunkX, quarryChunkZ, true);
        forcedChunkX = quarryChunkX;
        forcedChunkZ = quarryChunkZ;
    }

    private void tryMineOneBlock(ServerLevel level, ServerLevel quarryLevel) {
        ensureAssignedChunk();
        if (!isCursorInAssignedChunk()) {
            resetCursor(quarryLevel);
        }

        final int maxSkipsPerTick = 512;
        BlockPos targetPos = null;
        BlockState targetState = null;
        for (int i = 0; i < maxSkipsPerTick; i++) {
            targetPos = new BlockPos(mineX, mineY, mineZ);
            targetState = quarryLevel.getBlockState(targetPos);
            if (targetState.isAir() || targetState.is(Blocks.BEDROCK)) {
                advanceDownOrNextColumn(quarryLevel);
                continue;
            }
            break;
        }

        if (targetState.isAir() || targetState.is(Blocks.BEDROCK)) {
            return;
        }

        int cost = DEFAULT_RF_PER_BLOCK;
        if (energyStorage.getEnergyStored() < cost) return;

        var fluidState = quarryLevel.getFluidState(targetPos);
        if (!fluidState.isEmpty()) {
            if (fluidState.isSource()) {
                FluidStack toFill = new FluidStack(fluidState.getType(), 1000);
                int filled = fluidTank.fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    energyStorage.extractEnergy(cost, false);
                    quarryLevel.setBlock(targetPos, REPLACE_WITH.defaultBlockState(), 3);
                    blocksMined = blocksMined.add(BigInteger.ONE);
                    advanceDownOrNextColumn(quarryLevel);
                    setChanged();
                    syncToClient();
                    return;
                }
            }

            advanceDownOrNextColumn(quarryLevel);
            return;
        }

        ItemStack tool = buildMiningTool();
        var lootParams = new LootParams.Builder(quarryLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(targetPos))
                .withParameter(LootContextParams.TOOL, tool);
        var drops = targetState.getDrops(lootParams);

        for (ItemStack drop : drops) {
            ItemStack remaining = insertIntoExport(drop);
            if (!remaining.isEmpty()) {
                return;
            }
        }

        energyStorage.extractEnergy(cost, false);
        quarryLevel.setBlock(targetPos, REPLACE_WITH.defaultBlockState(), 3);
        blocksMined = blocksMined.add(BigInteger.ONE);
        advanceDownOrNextColumn(quarryLevel);
        setChanged();
        syncToClient();
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

    private boolean isCursorInAssignedChunk() {
        int cx = mineX >> 4;
        int cz = mineZ >> 4;
        return cx == quarryChunkX && cz == quarryChunkZ;
    }

    private void resetCursor(ServerLevel quarryLevel) {
        int startX = (quarryChunkX << 4);
        int startZ = (quarryChunkZ << 4);
        mineX = startX;
        mineZ = startZ;
        resetYForColumn(quarryLevel);
    }

    private void advanceDownOrNextColumn(ServerLevel quarryLevel) {
        mineY--;
        if (mineY >= quarryLevel.getMinBuildHeight()) {
            return;
        }

        advanceColumn(quarryLevel);
    }

    private void advanceColumn(ServerLevel quarryLevel) {
        int startX = (quarryChunkX << 4);
        int startZ = (quarryChunkZ << 4);

        int localX = mineX - startX;
        int localZ = mineZ - startZ;

        localX++;
        if (localX >= 16) {
            localX = 0;
            localZ++;
            if (localZ >= 16) {
                advanceToNextChunk(quarryLevel);
                return;
            }
        }

        mineX = startX + localX;
        mineZ = startZ + localZ;

        resetYForColumn(quarryLevel);
    }

    private void advanceToNextChunk(ServerLevel quarryLevel) {
        // Simple pattern: step +1 in X each time.
        quarryChunkX++;
        if (quarryChunkX == 0 && quarryChunkZ == 0) {
            quarryChunkX = 1;
        }

        forceChunkLoaded(quarryLevel, true);
        resetCursor(quarryLevel);
        setChanged();
        syncToClient();
    }

    private void resetYForColumn(ServerLevel quarryLevel) {
        int surface = quarryLevel.getHeight(Heightmap.Types.WORLD_SURFACE, mineX, mineZ);
        int y = surface - 1;
        if (y < quarryLevel.getMinBuildHeight()) {
            y = quarryLevel.getMinBuildHeight();
        }
        mineY = y;
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
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag energyTag = new CompoundTag();
        energyStorage.save(energyTag);
        tag.put("Energy", energyTag);
        CompoundTag itemsTag = new CompoundTag();
        exportItems.save(itemsTag, registries);
        tag.put("Items", itemsTag);

        tag.put("Tank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.put("QuarrySlots", quarrySlots.serializeNBT(registries));
        tag.putBoolean("Formed", formed);

        tag.putBoolean("Active", active);
        tag.putInt("QuarryChunkX", quarryChunkX);
        tag.putInt("QuarryChunkZ", quarryChunkZ);
        tag.putInt("MineX", mineX);
        tag.putInt("MineY", mineY);
        tag.putInt("MineZ", mineZ);
        if (selectedBiomeId != null) {
            tag.putString("SelectedBiome", selectedBiomeId);
        }

        tag.putString("BlocksMined", blocksMined.toString());
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
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

        active = tag.getBoolean("Active");
        quarryChunkX = tag.getInt("QuarryChunkX");
        quarryChunkZ = tag.getInt("QuarryChunkZ");
        mineX = tag.getInt("MineX");
        mineY = tag.getInt("MineY");
        mineZ = tag.getInt("MineZ");
        selectedBiomeId = tag.contains("SelectedBiome") ? tag.getString("SelectedBiome") : null;

        if (tag.contains("BlocksMined")) {
            try {
                blocksMined = new BigInteger(tag.getString("BlocksMined"));
            } catch (NumberFormatException ignored) {
                blocksMined = BigInteger.ZERO;
            }
        } else {
            blocksMined = BigInteger.ZERO;
        }
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
