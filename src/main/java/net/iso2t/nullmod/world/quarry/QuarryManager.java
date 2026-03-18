package net.iso2t.nullmod.world.quarry;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.iso2t.nullmod.core.Null;

import java.math.BigInteger;

/**
 * Manages operations and state for a quarry, including chunk loading, mining position tracking,
 * biome selection, and energy usage. This class handles the logic necessary for the quarry's
 * automated resource extraction process, ensuring systematic progression through assigned chunks.
 */
public class QuarryManager {

    private static final Block REPLACE_WITH = Blocks.BEDROCK;

    private static final int DEFAULT_RF_PER_BLOCK = 20_000;

    private static final ResourceKey<Level> QUARRY_OVERWORLD = ResourceKey.create(Registries.DIMENSION, Null.getResource("quarry_overworld"));
    private static final ResourceKey<Level> QUARRY_NETHER = ResourceKey.create(Registries.DIMENSION, Null.getResource("quarry_nether"));
    private static final ResourceKey<Level> QUARRY_END = ResourceKey.create(Registries.DIMENSION, Null.getResource("quarry_end"));

    private int forcedChunkX = Integer.MIN_VALUE;
    private int forcedChunkZ = Integer.MIN_VALUE;

    @Getter
    private boolean active;

    private long lastInstanceSalt;

    private int quarryChunkX;
    private int quarryChunkZ;

    @Getter
    private int mineX;

    @Getter
    private int mineY;

    @Getter
    private int mineZ;

    @Getter
    private BigInteger blocksMined = BigInteger.ZERO;

    @Getter
    private String selectedBiomeId;

    @Getter
    private String status;

    public static ResourceKey<Level> quarryLevelKey() {
        return QUARRY_OVERWORLD;
    }

    /**
     * Sets the active state of the instance and performs related actions based on the given parameters.
     *
     * @param active The desired active state to set for the instance.
     * @param overworld The ServerLevel instance representing the overworld, used to interact with other levels.
     */
    public void setActive(boolean active, ServerLevel overworld) {
        if (this.active == active) return;
        this.active = active;

        if (!active) {
            ServerLevel quarryLevel = overworld.getServer().getLevel(QUARRY_OVERWORLD);
            if (quarryLevel != null) {
                forceChunkLoaded(quarryLevel, false);
            }
        }
    }

    /**
     * Performs a single tick operation for the quarry entity, managing its mining process and associated state.
     *
     * @param overworld The primary server-level instance representing the overworld.
     * @param access An interface providing access to quarry-specific operations and data, such as the quarry controller position.
     */
    public void tick(ServerLevel overworld, QuarryMiningAccess access) {
        if (!active) {
            setActive(true, overworld);
        }

        lastInstanceSalt = access.getQuarryInstanceSalt();

        ensureAssignedChunk(overworld, overworld.getSeed(), access.getQuarryControllerPos(), lastInstanceSalt);
        if (mineY == 0) {
            initializeCursorIfNeeded(overworld, access.getQuarryControllerPos(), lastInstanceSalt);
        }

        ServerLevel quarryLevel = overworld.getServer().getLevel(QUARRY_OVERWORLD);
        if (quarryLevel == null) {
            updateStatus(access, "Quarry dimension missing");
            return;
        }

        forceChunkLoaded(quarryLevel, true);
        tryMineOneBlock(overworld, quarryLevel, access);
    }

    private void updateStatus(QuarryMiningAccess access, String next) {
        if (next != null && next.equals(status)) return;
        status = next;
        access.markDirtyAndSync();
    }

    /**
     * Ensures that a chunk is assigned for a quarry operation. If the quarry chunk coordinates
     * have not been previously set, this method calculates and assigns them based on the provided
     * seed and positional salt.
     *
     * @param overworld The server-level world instance, representing the overworld. This parameter
     *                  is currently unused but typically represents the context where the quarry
     *                  resides.
     * @param seed The long value used to influence the calculation of the quarry's chunk coordinates.
     * @param posSalt The positional salt value as a {@link BlockPos}, which is used in combination
     *                with the seed to compute a deterministic, unique chunk assignment.
     */
    public void ensureAssignedChunk(ServerLevel overworld, long seed, BlockPos posSalt, long instanceSalt) {
        if (quarryChunkX != 0 || quarryChunkZ != 0) return;

        long mixed = mix64(instanceSalt ^ (seed * 0x9E3779B97F4A7C15L));
        int baseX = (int) mixed;
        int baseZ = (int) (mixed >>> 32);

        int startX = (baseX & 0x7FFF) * 2;
        int startZ = (baseZ & 0x7FFF) * 2;

        if (startX == 0 && startZ == 0) {
            startX = 2;
        }

        int stepX = ((baseX >>> 16) | 1);
        int stepZ = ((baseZ >>> 16) | 1);
        int[] free = claimNextFreeChunk(overworld, startX, startZ, stepX, stepZ);
        quarryChunkX = free[0];
        quarryChunkZ = free[1];
    }

    private static int[] claimNextFreeChunk(ServerLevel overworld, int startChunkX, int startChunkZ, int stepX, int stepZ) {
        QuarryChunkData data = QuarryChunkData.get(overworld);
        int x = startChunkX;
        int z = startChunkZ;

        final int maxAttempts = 1_000_000;
        for (int i = 0; i < maxAttempts; i++) {
            if (x == 0 && z == 0) {
                x++;
            }

            if (!data.isUsed(x, z)) {
                data.markUsed(x, z);
                return new int[]{x, z};
            }

            x += stepX;
            z += stepZ;
        }

        data.markUsed(x, z);
        return new int[]{x, z};
    }

    /**
     * Initializes the cursor position for mining if it has not been set already.
     * This method assigns the coordinates for mining based on the specified overworld instance,
     * a position salt, and the world seed. It also ensures the associated chunk is properly loaded
     * and sets up the initial Y-level for mining. Subsequent calls will have no effect if the cursor
     * has already been initialized.
     *
     * @param overworld The instance of the overworld where the quarry is located.
     *                  Used for accessing world information and seed.
     * @param posSalt   A position-dependent randomization salt used to derive the mining coordinates.
     */
    private void initializeCursorIfNeeded(ServerLevel overworld, BlockPos posSalt, long instanceSalt) {
        if (mineY != 0) return;
        ensureAssignedChunk(overworld, overworld.getSeed(), posSalt, instanceSalt);
        int startX = (quarryChunkX << 4);
        int startZ = (quarryChunkZ << 4);

        long mixed = mix64((((long) quarryChunkX) << 32) ^ (quarryChunkZ & 0xFFFFFFFFL) ^ instanceSalt ^ (overworld.getSeed() * 0xD6E8FEB86659FD93L));
        int localX = (int) (mixed & 15L);
        int localZ = (int) ((mixed >>> 4) & 15L);
        mineX = startX + localX;
        mineZ = startZ + localZ;

        ServerLevel quarryLevel = overworld.getServer().getLevel(QUARRY_OVERWORLD);
        if (quarryLevel == null) {
            mineY = overworld.getMaxBuildHeight() - 1;
            return;
        }

        resetYForColumn(quarryLevel);
    }

    /**
     * Applies a series of bit-level transformations to the given input value to produce
     * a highly mixed 64-bit output. This function is often used for hashing or to create
     * pseudo-random values.
     *
     * @param z The input value to be mixed.
     * @return A 64-bit value representing the mixed output derived from the input value.
     */
    private static long mix64(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public void updateSelectedBiome(ServerLevel overworld, ResourceLocation fallback) {
        selectedBiomeId = fallback == null ? null : fallback.toString();
    }

    /**
     * Updates the chunk loading status for the quarry operation. This method ensures that the chunk
     * where the quarry is currently located is force-loaded, preventing it from being unloaded by
     * the game while the quarry is active. If the `forced` parameter is false, any previously
     * force-loaded chunk is released.
     *
     * @param quarryLevel The server-level instance representing the level where the quarry operates.
     * @param forced      A flag indicating whether the current quarry chunk should be force-loaded
     *                    (true) or released (false).
     */
    private void forceChunkLoaded(ServerLevel quarryLevel, boolean forced) {
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

    /**
     * Attempts to mine a single block within the assigned chunk for the quarry. This method selects
     * a target block based on the current cursor location, checks for mining conditions,
     * and processes the block removal, loot collection, and energy consumption. If the current block
     * cannot be mined, the cursor advances to the next valid block position.
     *
     * @param level        The main server-level instance where the quarry operates, typically for related operations and context.
     * @param quarryLevel  The server-level instance representing the specific dimension or level of the quarry operation.
     * @param access       An interface providing access to the quarry's energy storage, fluid tank, mining tools, and export logic.
     */
    private void tryMineOneBlock(ServerLevel level, ServerLevel quarryLevel, QuarryMiningAccess access) {
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
            updateStatus(access, "No blocks (skipping)");
            return;
        }

        int cost = DEFAULT_RF_PER_BLOCK;
        if (access.getEnergyStorage().getEnergyStored() < cost) {
            updateStatus(access, "No energy");
            return;
        }

        var fluidState = quarryLevel.getFluidState(targetPos);
        if (!fluidState.isEmpty()) {
            if (fluidState.isSource()) {
                FluidStack toFill = new FluidStack(fluidState.getType(), 1000);
                int filled = access.getFluidTank().fill(toFill, IFluidHandler.FluidAction.EXECUTE);
                if (filled == 1000) {
                    updateStatus(access, "Mining");
                    access.getEnergyStorage().extractEnergy(cost, false);
                    quarryLevel.setBlock(targetPos, REPLACE_WITH.defaultBlockState(), 3);
                    blocksMined = blocksMined.add(BigInteger.ONE);
                    advanceDownOrNextColumn(quarryLevel);
                    access.markDirtyAndSync();
                    return;
                }

                updateStatus(access, "Fluid tank full (skipping source fluids)");
                advanceDownOrNextColumn(quarryLevel);
                return;
            }

            updateStatus(access, "Skipping flowing fluid");
            advanceDownOrNextColumn(quarryLevel);
            return;
        }

        ItemStack tool = access.buildMiningTool();
        var lootParams = new LootParams.Builder(quarryLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(targetPos))
                .withParameter(LootContextParams.TOOL, tool);
        var drops = targetState.getDrops(lootParams);

        for (ItemStack drop : drops) {
            ItemStack remaining = access.insertIntoExport(drop);
            if (!remaining.isEmpty()) {
                updateStatus(access, "Export full");
                return;
            }
        }

        updateStatus(access, "Mining");
        access.getEnergyStorage().extractEnergy(cost, false);
        quarryLevel.setBlock(targetPos, REPLACE_WITH.defaultBlockState(), 3);
        blocksMined = blocksMined.add(BigInteger.ONE);
        advanceDownOrNextColumn(quarryLevel);
        access.markDirtyAndSync();
    }

    /**
     * Determines whether the current cursor position for the quarry is within the assigned chunk.
     * The cursor's x and z coordinates are compared against the x and z coordinates of the chunk
     * assigned for the quarry operation.
     *
     * @return true if the cursor is within the assigned chunk; false otherwise.
     */
    private boolean isCursorInAssignedChunk() {
        int cx = mineX >> 4;
        int cz = mineZ >> 4;
        return cx == quarryChunkX && cz == quarryChunkZ;
    }

    /**
     * Resets the internal mining cursor for the quarry to the starting position in the assigned chunk.
     * This method recalculates the cursor's initial x and z coordinates based on the chunk position
     * and initializes the y-coordinate to the appropriate value for the current column.
     *
     * @param quarryLevel The server-level instance representing the specific dimension or level
     *                    where the quarry operates. Used to determine height and boundary conditions
     *                    for resetting the cursor's y-coordinate.
     */
    private void resetCursor(ServerLevel quarryLevel) {
        int startX = (quarryChunkX << 4);
        int startZ = (quarryChunkZ << 4);
        mineX = startX;
        mineZ = startZ;
        resetYForColumn(quarryLevel);
    }

    /**
     * Advances the quarry's mining cursor downward in the current column. If the
     * cursor has reached the minimum build height, it moves to the next column to
     * continue the mining operation. This method ensures that mining progresses
     * systematically through the quarry's assigned area.
     *
     * @param quarryLevel The server-level instance representing the specific dimension
     *                    or level in which the quarry is operating. Used to determine
     *                    the minimum build height and handle transitions to the next
     *                    column.
     */
    private void advanceDownOrNextColumn(ServerLevel quarryLevel) {
        mineY--;
        if (mineY >= quarryLevel.getMinBuildHeight()) {
            return;
        }

        advanceColumn(quarryLevel);
    }

    /**
     * Advances the quarry's mining cursor to the next column within the currently assigned chunk.
     * If the end of the current chunk is reached, this method transitions the cursor to the
     * next chunk in sequence. The column is determined based on the local x and z coordinates
     * and progresses logically within the chunk boundaries.
     *
     * @param quarryLevel The server-level instance representing the specific dimension or level
     *                    in which the quarry is operating. Used to manage chunk boundaries
     *                    and handle transitions between chunks.
     */
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

    /**
     * Moves the quarry operation to the next chunk by incrementing the internal chunk coordinates
     * and updates related states. This method ensures the quarry avoids the origin chunk (0, 0)
     * and applies required updates to enforce chunk loading and reset the mining cursor.
     *
     * @param quarryLevel The server-level instance representing the specific dimension or level
     *                    where the quarry operates. Used to manage chunk loading and cursor resetting.
     */
    private void advanceToNextChunk(ServerLevel quarryLevel) {
        ServerLevel overworld = quarryLevel.getServer().overworld();
        long basis = (((long) quarryChunkX) << 32) ^ (quarryChunkZ & 0xFFFFFFFFL) ^ lastInstanceSalt ^ overworld.getSeed() ^ blocksMined.longValue();
        long mixed = mix64(basis);
        int baseX = (int) mixed;
        int baseZ = (int) (mixed >>> 32);

        int startX = (baseX & 0x7FFF) * 2;
        int startZ = (baseZ & 0x7FFF) * 2;
        if (startX == 0 && startZ == 0) {
            startX = 2;
        }

        int stepX = ((baseX >>> 16) | 1);
        int stepZ = ((baseZ >>> 16) | 1);
        int[] free = claimNextFreeChunk(overworld, startX, startZ, stepX, stepZ);
        quarryChunkX = free[0];
        quarryChunkZ = free[1];

        forceChunkLoaded(quarryLevel, true);
        resetCursor(quarryLevel);
    }

    /**
     * Resets the current y-coordinate for the mining cursor within the assigned column of the quarry.
     * The method determines the initial y-position for the column based on the heightmap of the specified
     * quarry level. If the calculated y-coordinate falls below the minimum build height of the level,
     * it is clamped to the minimum build height.
     *
     * @param quarryLevel The server-level instance representing the dimension or level where the quarry
     *                    operates. Used to determine the surface height and boundary conditions for the
     *                    y-coordinate.
     */
    private void resetYForColumn(ServerLevel quarryLevel) {
        int surface = quarryLevel.getHeight(Heightmap.Types.WORLD_SURFACE, mineX, mineZ);
        int y = surface - 1;
        if (y < quarryLevel.getMinBuildHeight()) {
            y = quarryLevel.getMinBuildHeight();
        }
        mineY = y;
    }

    /**
     * Saves the additional data related to the quarry's state and configuration to the provided tag.
     * This includes the active state, coordinates for the quarry's operation, a biome ID if selected,
     * and the count of blocks mined. The data is written to the `CompoundTag` for persistence and
     * later retrieval.
     *
     * @param tag        The tag to which the quarry's state and configuration data will be saved.
     * @param registries The registry provider used for interacting with the current registry system.
     */
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putBoolean("Active", active);
        tag.putInt("QuarryChunkX", quarryChunkX);
        tag.putInt("QuarryChunkZ", quarryChunkZ);
        tag.putInt("MineX", mineX);
        tag.putInt("MineY", mineY);
        tag.putInt("MineZ", mineZ);
        if (selectedBiomeId != null) {
            tag.putString("SelectedBiome", selectedBiomeId);
        }
        if (status != null) {
            tag.putString("Status", status);
        }
        tag.putString("BlocksMined", blocksMined.toString());
    }

    /**
     * Loads the additional data related to the quarry's state and configuration from the provided tag.
     * This includes the active state, coordinates for the quarry's operation, a biome ID if selected,
     * and the count of blocks mined. The data is read from the `CompoundTag` to restore the quarry's state.
     *
     * @param tag        The tag containing the serialized data for the quarry's state and configuration.
     * @param registries The registry provider used for interacting with the current registry system.
     */
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        active = tag.getBoolean("Active");
        quarryChunkX = tag.getInt("QuarryChunkX");
        quarryChunkZ = tag.getInt("QuarryChunkZ");
        mineX = tag.getInt("MineX");
        mineY = tag.getInt("MineY");
        mineZ = tag.getInt("MineZ");
        selectedBiomeId = tag.contains("SelectedBiome") ? tag.getString("SelectedBiome") : null;
        status = tag.contains("Status") ? tag.getString("Status") : null;

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
}
