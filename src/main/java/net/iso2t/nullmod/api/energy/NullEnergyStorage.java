package net.iso2t.nullmod.api.energy;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.energy.EnergyStorage;

import java.util.Objects;

public class NullEnergyStorage extends EnergyStorage {

    private final Runnable onChanged;

    public NullEnergyStorage(int capacity) {
        this(capacity, capacity, capacity, 0, null);
    }

    public NullEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0, null);
    }

    public NullEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy, Runnable onChanged) {
        super(capacity, maxReceive, maxExtract, energy);
        this.onChanged = onChanged;
    }

    public CompoundTag save(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        tag.putInt("Energy", this.energy);
        tag.putInt("Capacity", this.capacity);
        return tag;
    }

    public void load(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        this.energy = Math.min(tag.getInt("Energy"), this.capacity);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            onChanged();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            onChanged();
        }
        return extracted;
    }

    public void setEnergy(int energy) {
        int clamped = Math.max(0, Math.min(energy, this.capacity));
        if (this.energy != clamped) {
            this.energy = clamped;
            onChanged();
        }
    }

    private void onChanged() {
        if (this.onChanged != null) {
            this.onChanged.run();
        }
    }
}
