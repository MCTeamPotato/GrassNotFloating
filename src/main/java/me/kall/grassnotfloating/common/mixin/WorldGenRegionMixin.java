package me.kall.grassnotfloating.common.mixin;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import me.kall.grassnotfloating.GrassNotFloating;
import me.kall.grassnotfloating.common.api.IBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin implements LevelReader {
    @Inject(method = "setBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/WorldGenRegion;getChunk(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/chunk/ChunkAccess;", shift = At.Shift.AFTER))
    private void noGrass(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (((IBlock)state.getBlock()).grassnotfloating$unfloatable()) {
            long chunkPos = this.getChunk(pos).getPos().toLong();
            GrassNotFloating.POSITIONS.computeIfAbsent(chunkPos, posLong -> new LongOpenHashSet()).add(pos.asLong());
        }
    }
}
