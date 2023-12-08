package com.teampotato.grassnotfloating.mixin;

import com.teampotato.grassnotfloating.api.Floatable;
import net.minecraft.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class MixinBlock implements Floatable {
    @Unique private boolean grassNotFloating$shouldNotFloat;

    @Override
    public boolean grassNotFloating$shouldNotFloat() {
        return this.grassNotFloating$shouldNotFloat;
    }

    @Override
    public void grassNotFloating$setShouldNotFloat(boolean shouldNotFloat) {
        this.grassNotFloating$shouldNotFloat = shouldNotFloat;
    }
}