package me.kall.grassnotfloating.mixin;

import me.kall.grassnotfloating.api.Trackable;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Block.class)
public class BlockMixin implements Trackable {
    @Unique private boolean float$tracked;

    @Override
    public boolean float$tracked() {
        return this.float$tracked;
    }

    @Override
    public void float$setTracked(boolean tracked) {
        this.float$tracked = tracked;
    }
}
