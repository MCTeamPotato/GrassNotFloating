package me.kall.grassnotfloating.data;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class PendingRemoval extends SavedData {
    private static final String DATA_NAME = "grassnotfloating_pending_removal";
    public final Object2ObjectMap<ResourceLocation, LongSet> positions = new Object2ObjectOpenHashMap<>();

    public static @NotNull PendingRemoval get(@NotNull ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PendingRemoval::load, PendingRemoval::new, DATA_NAME);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        for (Map.Entry<ResourceLocation, LongSet> entry : positions.entrySet()) {
            ListTag list = new ListTag();
            for (long pos : entry.getValue()) {
                list.add(LongTag.valueOf(pos));
            }
            tag.put(entry.getKey().toString(), list);
        }
        return tag;
    }

    public static @NotNull PendingRemoval load(@NotNull CompoundTag tag) {
        PendingRemoval removal = new PendingRemoval();
        for (String dimKey : tag.getAllKeys()) {
            ResourceLocation dim = ResourceLocation.tryParse(dimKey);
            if (dim == null) continue;
            ListTag list = tag.getList(dimKey, 4);
            LongSet set = new LongOpenHashSet();
            for (net.minecraft.nbt.Tag value : list) {
                set.add(((LongTag) value).getAsLong());
            }
            removal.positions.put(dim, set);
        }
        return removal;
    }
}
