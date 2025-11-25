package me.kall.grassnotfloating;

import me.kall.duplicationless.data.ChunkData;
import me.kall.duplicationless.event.BlockChangeEvent;
import me.kall.duplicationless.util.Executor;
import me.kall.grassnotfloating.config.FloatConfig;
import me.kall.grassnotfloating.data.Unfloatable;
import me.kall.grassnotfloating.ext.Trackable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Supplier;

@Mod(GrassNotFloating.MOD_ID)
public final class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";
    public static final Logger LOGGER = LogManager.getLogger(GrassNotFloating.class);

    private static final Supplier<BlockState> AIR = Blocks.AIR::defaultBlockState;

    public GrassNotFloating(@NotNull FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.addListener((ModConfigEvent.Loading event) -> FloatConfig.configLoad(event));
        modBus.addListener((ModConfigEvent.Reloading event) -> FloatConfig.configLoad(event));

        forgeBus.addListener(this::blockChange);
        forgeBus.addListener(this::loadChunk);

        context.registerConfig(ModConfig.Type.COMMON, FloatConfig.INSTANCE);
    }

    private void blockChange(@NotNull BlockChangeEvent event) {
        ServerLevel level = event.level();
        long chunk = event.chunkPos();
        long block = event.blockPos();
        if (((Trackable)event.oldState().getBlock()).float$tracked()) level.getServer().execute(() -> Unfloatable.get(level).remove(chunk, block));
        if (((Trackable)event.newState().getBlock()).float$tracked()) level.getServer().execute(() -> Unfloatable.get(level).add(chunk, block));
    }

    @SuppressWarnings("PatternVariableCanBeUsed")
    private void loadChunk(ChunkEvent.@NotNull Load event) {
        ChunkPos chunkPos = event.getChunk().getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        long chunkKey = chunkPos.toLong();
        if (event.getLevel() instanceof ServerLevel) {
            ServerLevel level = (ServerLevel) event.getLevel();
            ChunkData<Long, BlockState> unfloatable = Unfloatable.get(level);
            Set<Long> tracked = unfloatable.viewChunk(chunkKey);
            if (tracked.isEmpty()) return;
            level.getServer().execute(new AirDetect(tracked, level, chunkX, chunkZ, AIR.get(), chunkKey, unfloatable));
        }
    }

    private record AirDetect(Set<Long> tracked, ServerLevel level, int chunkX, int chunkZ, BlockState air, long chunkKey, ChunkData<Long, BlockState> unfloatable) implements Runnable {
        @Override
        public void run() {
            for (long pos : tracked) {
                BlockPos.MutableBlockPos potentialAirPos = new BlockPos.MutableBlockPos(BlockPos.getX(pos), BlockPos.getY(pos) - 1, BlockPos.getZ(pos));
                if (!level.isEmptyBlock(potentialAirPos)) continue;

                potentialAirPos.setY(potentialAirPos.getY() + 1);

                while (((Trackable) level.getBlockState(potentialAirPos).getBlock()).float$tracked()) {
                    Executor.runAfter(1, new Removal(level, chunkX, chunkZ, potentialAirPos.immutable(), air));
                    potentialAirPos.setY(potentialAirPos.getY() + 1);
                }
            }

            unfloatable.data().remove(chunkKey);
        }
    }

    private record Removal(ServerLevel level, int chunkX, int chunkZ, BlockPos airPos, BlockState air) implements Runnable {
        @Override
        public void run() {
            if (level.hasChunk(chunkX, chunkZ)) {
                LOGGER.info("Removing float {} at {}", level.getBlockState(airPos).getBlock().toString(), airPos);
                level.setBlockAndUpdate(airPos, air);
            } else {
                Executor.runAfter(1, this);
            }
        }
    }
}
