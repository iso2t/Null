package net.iso2t.nullmod.world.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * Provides access to critical components and operations of a quarry mining system.
 * This interface defines methods to retrieve the quarry controller position, access
 * energy and fluid storage, manage mining tools, export resources, and synchronize
 * the system state.
 */
public interface QuarryMiningAccess {

    /**
     * Retrieves the position of the quarry controller block in the world.
     *
     * @return A {@code BlockPos} object representing the coordinates of the quarry controller.
     */
    BlockPos getQuarryControllerPos();

    long getQuarryInstanceSalt();

    /**
     * Retrieves the energy storage associated with the quarry system.
     *
     * @return An {@code IEnergyStorage} instance representing the energy storage unit
     *         that can be used to monitor or manipulate the energy levels of the quarry system.
     */
    IEnergyStorage getEnergyStorage();

    /**
     * Retrieves the fluid storage tank associated with the quarry system.
     *
     * @return A {@code FluidTank} instance representing the fluid storage unit of the quarry system,
     *         which can be used to monitor or manipulate fluid levels.
     */
    FluidTank getFluidTank();

    /**
     * Constructs and retrieves a new mining tool specifically designed for the quarry system.
     * The tool is configured to interact with the quarry's mining operations, enabling
     * resource extraction and other related tasks.
     *
     * @return An {@code ItemStack} that represents the newly created mining tool
     *         tailored for use in the quarry system.
     */
    ItemStack buildMiningTool();

    /**
     * Inserts the provided {@code ItemStack} into the quarry's export system. This method is
     * used to transfer items from the quarry system to an external storage or processing pipeline.
     *
     * @param stack The {@code ItemStack} to be inserted into the export system. The stack
     *              must not be null and should represent the items to be exported.
     * @return An {@code ItemStack} representing the remaining items that could not be
     *         inserted due to capacity limitations or other restrictions. If the entire
     *         stack is successfully inserted, an empty {@code ItemStack} will be returned.
     */
    ItemStack insertIntoExport(ItemStack stack);

    /**
     * Marks the quarry system as requiring an update and synchronizes its state with connected systems.
     * This method should be called whenever a significant change occurs in the system's data, such
     * as modifications to energy storage, fluid storage, or mining operations. This is to ensure that the
     * changes are properly saved and propagated to other components or systems.
     */
    void markDirtyAndSync();
}
