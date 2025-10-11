package me.kall.grassnotfloating;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import me.kall.grassnotfloating.api.Trackable;
import me.kall.grassnotfloating.data.BlockTracker;
import me.kall.grassnotfloating.data.PendingRemoval;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mod(GrassNotFloating.MOD_ID)
public final class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";
    public static final Logger LOGGER = LogManager.getLogger(GrassNotFloating.class);

    private static final Supplier<BlockState> AIR = Suppliers.memoize(Blocks.AIR::defaultBlockState);

    public GrassNotFloating(@NotNull FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, Config.INSTANCE);
        context.getModEventBus().addListener((FMLCommonSetupEvent event) -> initConfig());
        MinecraftForge.EVENT_BUS.addListener(this::onChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(this::onLevelTick);
    }

    public void onChunkLoad(ChunkEvent.@NotNull Load event) {
        long chunkKey = event.getChunk().getPos().toLong();
        if (event.getLevel() instanceof ServerLevel level && event.isNewChunk()) {
            level.getServer().executeIfPossible(() -> {
                ResourceLocation dim = level.dimension().location();
                LongSet tracked = BlockTracker.get(dim, chunkKey, level);
                if (tracked.isEmpty()) return;
                for (long pos : tracked) {
                    BlockPos.MutableBlockPos potentialAirPos = new BlockPos.MutableBlockPos(BlockPos.getX(pos), BlockPos.getY(pos) - 1, BlockPos.getZ(pos));
                    if (level.isEmptyBlock(potentialAirPos)) {
                        potentialAirPos.setY(potentialAirPos.getY() + 1);
                        while (((Trackable)level.getBlockState(potentialAirPos).getBlock()).float$tracked()) {
                            PendingRemoval.get(level).positions.computeIfAbsent(dim, key -> new LongOpenHashSet()).add(potentialAirPos.asLong());
                            potentialAirPos.setY(potentialAirPos.getY() + 1);
                        }
                    }
                }

                BlockTracker.get(level).trackedBlocks.get(dim).remove(chunkKey);
                BlockTracker.get(level).setDirty();
            });
        }
    }

    public void onLevelTick(TickEvent.@NotNull LevelTickEvent event) {
        try {
            if (event.level instanceof ServerLevel level && event.phase == TickEvent.Phase.START && level.getServer().getTickCount() % 20 == 0) {
                level.getServer().executeIfPossible(() -> {
                    ResourceLocation dim = level.dimension().location();
                    PendingRemoval toRemove = PendingRemoval.get(level);
                    LongSet blocks = toRemove.positions.get(dim);
                    if (blocks == null || blocks.isEmpty()) return;
                    long[] positionsArray = blocks.toLongArray();
                    BlockState air = AIR.get();
                    for (long posLong : positionsArray) {
                        BlockPos pos = BlockPos.of(posLong);
                        if (!level.isLoaded(pos)) continue;
                        level.setBlockAndUpdate(pos, air);
                        blocks.remove(posLong);
                        LOGGER.info("Removed floating block at {}", pos);
                    }
                    if (blocks.isEmpty()) toRemove.positions.remove(dim);
                    toRemove.setDirty();
                });
            }
        } catch (Exception e) {
            LOGGER.error("Error removing floating blocks", e);
        }
    }

    private static void initConfig() {
        Config.TRACKED.get().forEach(name -> {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.tryParse(name));
            if (block != null) ((Trackable)block).float$setTracked(true);
        });
    }

    public static final class Config {
        public static final ForgeConfigSpec INSTANCE;
        public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRACKED;

        static {
            ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
            builder.push(GrassNotFloating.MOD_ID);
            TRACKED = builder.defineList("UnfloatableBlocks", Lists.newArrayList("minecraft:grass", "minecraft:fern", "minecraft:tall_grass"), Predicates.alwaysTrue());
            builder.pop();
            INSTANCE = builder.build();
        }
    }
}
