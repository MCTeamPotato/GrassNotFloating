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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Mod(GrassNotFloating.MOD_ID)
public final class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";
    public static final Logger LOGGER = LogManager.getLogger(GrassNotFloating.class);

    private static final Supplier<BlockState> AIR = Blocks.AIR::defaultBlockState;

    public GrassNotFloating() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        modBus.addListener((FMLCommonSetupEvent event) -> FloatConfig.initConfig());
        modBus.addListener(FloatConfig::configLoad);

        forgeBus.addListener(this::dataRebuild);
        forgeBus.addListener(this::blockChange);
        forgeBus.addListener(this::chunkLoad);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FloatConfig.INSTANCE);
    }

    private void dataRebuild(@NotNull FMLServerStartedEvent event) {
        event.getServer().execute(() -> event.getServer().getAllLevels().forEach(level -> Unfloatable.get(level).rebuild(level)));
    }

    private void blockChange(@NotNull BlockChangeEvent event) {
        ServerLevel level = event.level();
        long chunk = event.chunkPos();
        long block = event.blockPos();
        if (((Trackable)event.oldState().getBlock()).float$tracked()) level.getServer().execute(() -> Unfloatable.get(level).remove(level, chunk, block));
        if (((Trackable)event.newState().getBlock()).float$tracked()) level.getServer().execute(() -> Unfloatable.get(level).add(level, chunk, block));
    }

    private void chunkLoad(ChunkEvent.@NotNull Load event) {
        ChunkPos chunkPos = event.getChunk().getPos();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;
        long chunkKey = chunkPos.toLong();
        if (event.getWorld() instanceof ServerLevel) {
            ServerLevel level = (ServerLevel) event.getWorld();
            ChunkData<Long, BlockState> unfloatable = Unfloatable.get(level);
            Set<Long> tracked = unfloatable.viewChunk(level, chunkKey);
            if (tracked.isEmpty()) return;
            level.getServer().execute(new AirDetect(tracked, level, chunkX, chunkZ, AIR.get(), chunkKey, unfloatable));
        }
    }

    private static final class AirDetect implements Runnable {
        final Set<Long> tracked;
        final ServerLevel level;
        final int chunkX;
        final int chunkZ;
        final BlockState air;
        final long chunkKey;
        final ChunkData<Long, BlockState> unfloatable;

        private AirDetect(Set<Long> tracked, ServerLevel level, int chunkX, int chunkZ, BlockState air, long chunkKey, ChunkData<Long, BlockState> unfloatable) {
            this.tracked = tracked;
            this.level = level;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.air = air;
            this.chunkKey = chunkKey;
            this.unfloatable = unfloatable;
        }

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

            Optional.ofNullable(unfloatable.data().get(level.dimension().location())).ifPresent(map -> map.remove(chunkKey));
        }
    }

    private static class Removal implements Runnable {
        private final ServerLevel level;
        private final int chunkX;
        private final int chunkZ;
        private final BlockPos airPos;
        private final BlockState air;
        private int retries = 0;

        public Removal(ServerLevel level, int chunkX, int chunkZ, BlockPos airPos, BlockState air) {
            this.level = level;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.airPos = airPos;
            this.air = air;
        }

        @Override
        public void run() {
            if (retries > 20) {
                GrassNotFloating.LOGGER.warn("Gave up removing float at {} after {} retries", airPos, retries);
                return;
            }

            if (level.hasChunk(chunkX, chunkZ)) {
                Block block = level.getBlockState(airPos).getBlock();
                if (((Trackable)block).float$tracked()){
                    LOGGER.info("Removing float {} at {}", block, airPos);
                    level.setBlockAndUpdate(airPos, air);
                }
            } else {
                this.retries++;
                Executor.runAfter(1, this);
            }
        }
    }
}
