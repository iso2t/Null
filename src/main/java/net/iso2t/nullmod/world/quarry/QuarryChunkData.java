package net.iso2t.nullmod.world.quarry;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class QuarryChunkData extends SavedData {

    private static final String DATA_NAME = "nullmod_quarry_chunks";

    private final Set<Long> usedChunks = new HashSet<>();

    public static QuarryChunkData get(net.minecraft.server.level.ServerLevel overworld) {
        return overworld.getDataStorage().computeIfAbsent(new SavedData.Factory<>(QuarryChunkData::new, QuarryChunkData::load), DATA_NAME);
    }

    public boolean isUsed(int chunkX, int chunkZ) {
        return usedChunks.contains(pack(chunkX, chunkZ));
    }

    public boolean markUsed(int chunkX, int chunkZ) {
        long key = pack(chunkX, chunkZ);
        if (usedChunks.add(key)) {
            setDirty();
            return true;
        }
        return false;
    }

    private static long pack(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) ^ (chunkZ & 0xFFFFFFFFL);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        long[] arr = new long[usedChunks.size()];
        int i = 0;
        for (Long v : usedChunks) {
            arr[i++] = v;
        }
        tag.putLongArray("Used", arr);
        return tag;
    }

    public static QuarryChunkData load(CompoundTag tag, HolderLookup.Provider registries) {
        QuarryChunkData data = new QuarryChunkData();
        if (tag.contains("Used")) {
            for (long v : tag.getLongArray("Used")) {
                data.usedChunks.add(v);
            }
        }
        return data;
    }
}
