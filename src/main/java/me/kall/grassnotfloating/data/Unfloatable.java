package me.kall.grassnotfloating.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.kall.duplicationless.data.ChunkData;
import me.kall.grassnotfloating.ext.Trackable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

public class Unfloatable extends ChunkData.BlockData {
    private final Long2ObjectMap<Set<Long>> data = new Long2ObjectOpenHashMap<>();

    @Override
    public @NotNull Long2ObjectMap<Set<Long>> data() {
        return this.data;
    }

    @Override
    public boolean dataTrustable() {
        return false;
    }

    @Override
    public @Nullable Predicate<BlockState> validation() {
        return state -> ((Trackable)state.getBlock()).float$tracked();
    }

    public static @NotNull ChunkData<Long, BlockState> get(ServerLevel level) {
        return get(level, Unfloatable::new, "GrassNotFloatingBlockData");
    }
}
