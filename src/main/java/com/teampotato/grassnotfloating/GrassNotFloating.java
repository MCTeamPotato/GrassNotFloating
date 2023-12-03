package com.teampotato.grassnotfloating;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
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
