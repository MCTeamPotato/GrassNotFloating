package me.kall.grassnotfloating.mixin;

import me.kall.grassnotfloating.ext.DatRebuilder;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public class ServerLevelMixin implements DatRebuilder {

    @Unique
    private boolean data$rebuilt;

    @Override
    public boolean data$rebuilt() {
        return this.data$rebuilt;
    }

    @Override
    public void data$setRebuilt() {
        this.data$rebuilt = true;
    }
}
