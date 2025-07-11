package me.kall.grassnotfloating.common.mixin;

import me.kall.grassnotfloating.common.api.IBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements IBlock {
    @Unique
    private boolean grassnotfloating$unfloatable;

    @Override
    public boolean grassnotfloating$unfloatable() {
        return this.grassnotfloating$unfloatable;
    }

    @Override
    public void grassnotfloating$setUnfloatable() {
        this.grassnotfloating$unfloatable = true;
    }
}
