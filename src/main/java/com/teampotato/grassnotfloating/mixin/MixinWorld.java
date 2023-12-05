package com.teampotato.grassnotfloating.mixin;

import com.teampotato.grassnotfloating.GrassNotFloating;
import com.teampotato.grassnotfloating.api.Floatable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {
    @Shadow public abstract BlockState getBlockState(BlockPos pPos);

    @Shadow public abstract boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft);

    @Inject(method = "setBlock(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void grassNotFloating(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft, CallbackInfoReturnable<Boolean> cir) {
        if (pPos.getY() > 2 && this.getBlockState(pPos.below()).is(Blocks.AIR) && ((Floatable)pState.getBlock()).grassNotFloating$shouldNotFloat()) {
            cir.setReturnValue(setBlock(pPos, GrassNotFloating.AIR, pFlags, pRecursionLeft));
        }
    }
}
