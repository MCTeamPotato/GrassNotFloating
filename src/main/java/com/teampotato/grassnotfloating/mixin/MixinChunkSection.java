package com.teampotato.grassnotfloating.mixin;

import com.teampotato.grassnotfloating.GrassNotFloating;
import com.teampotato.grassnotfloating.api.Floatable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSection.class)
public abstract class MixinChunkSection {
    @Shadow public abstract BlockState getBlockState(int pX, int pY, int pZ);

    @Shadow public abstract BlockState setBlockState(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks);

    @Inject(method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At("HEAD"), cancellable = true)
    private void grassNotFloating(int pX, int pY, int pZ, BlockState pState, boolean pUseLocks, CallbackInfoReturnable<BlockState> cir) {
        if (pY > 2 && this.getBlockState(pX, pY - 1, pZ).is(Blocks.AIR) && ((Floatable)pState.getBlock()).grassNotFloating$shouldNotFloat()) {
            cir.setReturnValue(setBlockState(pX, pY, pZ, GrassNotFloating.AIR, pUseLocks));
        }
    }
}
