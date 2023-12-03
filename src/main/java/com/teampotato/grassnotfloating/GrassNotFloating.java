package com.teampotato.grassnotfloating;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod(GrassNotFloating.MOD_ID)
public class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";

    public static boolean isGrass(@NotNull Block block) {
        ResourceLocation id = block.getRegistryName();
        if (id == null) return false;
        return id.toString().contains("grass");
    }

    public static final BlockState AIR = Blocks.AIR.defaultBlockState();
}
