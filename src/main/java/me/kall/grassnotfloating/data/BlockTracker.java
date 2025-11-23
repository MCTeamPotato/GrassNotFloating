package me.kall.grassnotfloating.data;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockTracker extends SavedData {
    private static final String DATA_NAME = "grassnotfloating_block_tracker";

    public final Object2ObjectMap<ResourceLocation, Long2ObjectMap<LongSet>> trackedBlocks = new Object2ObjectOpenHashMap<>();

    public static @NotNull BlockTracker get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(BlockTracker::load, BlockTracker::new, DATA_NAME);
    }

    public static void add(ServerLevel level, long chunkKey, long pos) {
        BlockTracker data = get(level);
        MinecraftServer server = level.getServer();
        if (server.isRunning()) {
            server.execute(() -> data.trackedBlocks.computeIfAbsent(level.dimension().location(), key -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkKey, key -> new LongOpenHashSet()).add(pos));
        } else {
            synchronized (data.trackedBlocks) {
                data.trackedBlocks.computeIfAbsent(level.dimension().location(), key -> new Long2ObjectOpenHashMap<>()).computeIfAbsent(chunkKey, key -> new LongOpenHashSet()).add(pos);
            }
        }
        data.setDirty();
    }

    public static LongSet get(ResourceLocation dim, long chunkKey, @NotNull ServerLevel level) {
        BlockTracker data = get(level);
        return data.trackedBlocks.getOrDefault(dim, Long2ObjectMaps.emptyMap()).getOrDefault(chunkKey, LongSets.emptySet());
    }

    public static @NotNull BlockTracker load(@NotNull CompoundTag tag) {
        BlockTracker tracker = new BlockTracker();
        ListTag dimList = tag.getList("dims", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < dimList.size(); i++) {
            CompoundTag dimTag = dimList.getCompound(i);
            Long2ObjectMap<LongSet> chunkMap = new Long2ObjectOpenHashMap<>();
            ListTag chunkList = dimTag.getList("chunks", CompoundTag.TAG_COMPOUND);

            for (int j = 0; j < chunkList.size(); j++) {
                CompoundTag chunkTag = chunkList.getCompound(j);
                LongOpenHashSet posSet = new LongOpenHashSet(chunkTag.getLongArray("positions"));
                chunkMap.put(chunkTag.getLong("key"), posSet);
            }

            tracker.trackedBlocks.put(ResourceLocation.tryParse(dimTag.getString("id")), chunkMap);
        }
        return tracker;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag dimList = new ListTag();

        for (Map.Entry<ResourceLocation, Long2ObjectMap<LongSet>> dimEntry : trackedBlocks.entrySet()) {
            CompoundTag dimTag = new CompoundTag();
            dimTag.putString("id", dimEntry.getKey().toString());
            ListTag chunkList = new ListTag();

            for (Long2ObjectMap.Entry<LongSet> chunkEntry : dimEntry.getValue().long2ObjectEntrySet()) {
                CompoundTag chunkTag = new CompoundTag();
                chunkTag.putLong("key", chunkEntry.getLongKey());
                chunkTag.put("positions", new LongArrayTag(chunkEntry.getValue().toLongArray()));
                chunkList.add(chunkTag);
            }

            dimTag.put("chunks", chunkList);
            dimList.add(dimTag);
        }

        tag.put("dims", dimList);
        return tag;
    }
}
