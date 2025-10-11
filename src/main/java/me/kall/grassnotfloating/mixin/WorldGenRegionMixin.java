package me.kall.grassnotfloating.mixin;

import me.kall.grassnotfloating.api.Trackable;
import me.kall.grassnotfloating.data.BlockTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class WorldGenRegionMixin {
    @Shadow @Final private ServerLevel level;

    @Inject(method = "setBlock", at = @At("RETURN"))
    private void onBlockSet(BlockPos pos, BlockState state, int flags, int recursionLeft, @NotNull CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (((Trackable)state.getBlock()).float$tracked()) {
            ResourceKey<Level> dim = this.level.dimension();
            long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
            BlockTracker.add(dim, chunkKey, pos.asLong(), this.level.getServer());
        }
    }
}
