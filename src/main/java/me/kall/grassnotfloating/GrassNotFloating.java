package me.kall.grassnotfloating;

import it.unimi.dsi.fastutil.longs.*;
import me.kall.grassnotfloating.common.api.IBlock;
import me.kall.grassnotfloating.common.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(GrassNotFloating.MOD_ID)
public final class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";
    public static final Long2ObjectMap<LongSet> POSITIONS = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private static final LongSet TO_REMOVE = LongSets.synchronize(new LongOpenHashSet());

    private static int tick = 40;

    public GrassNotFloating(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, Config.INSTANCE);
        MinecraftForge.EVENT_BUS.addListener((ChunkEvent.Load event) -> {
            long pos = event.getChunk().getPos().toLong();
            if (event.getLevel() instanceof ServerLevel serverLevel && event.isNewChunk()) {
                LongSet positions = POSITIONS.get(pos);
                if (positions == null || positions.isEmpty()) return;
                for (long position : positions) {
                    BlockPos.MutableBlockPos possibleAirPos = new BlockPos.MutableBlockPos(BlockPos.getX(position), BlockPos.getY(position) - 1, BlockPos.getZ(position));
                    if (serverLevel.getBlockState(possibleAirPos).isAir()) {
                        possibleAirPos.set(possibleAirPos.getX(), possibleAirPos.getY() + 1, possibleAirPos.getZ());
                        while (((IBlock)serverLevel.getBlockState(possibleAirPos).getBlock()).grassnotfloating$unfloatable()) {
                            TO_REMOVE.add(possibleAirPos.asLong());
                            possibleAirPos.set(possibleAirPos.getX(), possibleAirPos.getY() + 1, possibleAirPos.getZ());
                        }
                    }
                }
                POSITIONS.remove(pos);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((TickEvent.LevelTickEvent event) -> {
            if (event.level instanceof ServerLevel && !TO_REMOVE.isEmpty()) {
                tick--;
                if (tick <= 0) {
                    tick = 40;
                    clearBlock((ServerLevel)event.level);
                }
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStartingEvent event) -> initConfig());
    }

    private static void initConfig() {
        Config.UNFLOATABLE.get().forEach(name -> {
            Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(name));
            if (block != null) ((IBlock)block).grassnotfloating$setUnfloatable();
        });
    }

    private static void clearBlock(ServerLevel serverLevel) {
        BlockState air = Blocks.AIR.defaultBlockState();
        LongIterator iterator = TO_REMOVE.iterator();
        while (iterator.hasNext()) {
            BlockPos blockPos = BlockPos.of(iterator.nextLong());
            if (!serverLevel.isLoaded(blockPos)) continue;
            ChunkAccess chunkAccess = serverLevel.getChunk(blockPos);
            if (chunkAccess.getStatus().isOrAfter(ChunkStatus.FULL)) {
                serverLevel.setBlockAndUpdate(blockPos, air);
                iterator.remove();
            }
        }
    }
}
