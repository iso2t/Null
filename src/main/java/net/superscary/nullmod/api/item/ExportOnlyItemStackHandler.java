package net.superscary.nullmod.api.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ExportOnlyItemStackHandler extends ItemStackHandler {

    private final Runnable onChanged;

    public ExportOnlyItemStackHandler(int size, Runnable onChanged) {
        super(size);
        this.onChanged = onChanged;
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.merge(this.serializeNBT(registries));
        return tag;
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        this.deserializeNBT(registries, tag);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (onChanged != null) {
            onChanged.run();
        }
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    public @NotNull ItemStack forceInsertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }
}
