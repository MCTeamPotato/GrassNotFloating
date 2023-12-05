package com.teampotato.grassnotfloating;

import com.teampotato.grassnotfloating.api.Floatable;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod(GrassNotFloating.MOD_ID)
public class GrassNotFloating {
    public static final String MOD_ID = "grassnotfloating";

    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private static final ForgeConfigSpec CONFIG;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCKS_THAT_SHOULD_NOT_FLOAT, BLOCKS_THAT_SHOULD_NOT_FLOAT_NAMESPACE_ONLY, BLOCKS_THAT_SHOULD_NOT_FLOAT_PATH_ONLY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("GrassNotFloating").comment(
                "Detection is processed based on the registry name of the block.",
                "A registry name has three parts, namespace, path, and the separator colon",
                "for example, `minecraft:grass` is a registry name, `minecraft` is its namespace, `grass` is its path, `:` is the separator colon",
                "(1) and (2)'s values will be compare strictly and require an equal on detection",
                "for (3), it's not strictly but use a `contains` check. For things like potato_grass, it will also works well"
        );
        BLOCKS_THAT_SHOULD_NOT_FLOAT = builder.defineList("(1) Blocks That Should Not Float", ObjectArrayList.wrap(new String[]{"minecraft:grass"}), o -> true);
        BLOCKS_THAT_SHOULD_NOT_FLOAT_NAMESPACE_ONLY = builder.defineList("(2) Blocks That Should Not Float (Namespace Only)", ObjectArrayList.wrap(new String[]{}), o -> true);
        BLOCKS_THAT_SHOULD_NOT_FLOAT_PATH_ONLY = builder.defineList("(3) Blocks That Should Not Float (Path Only)", ObjectArrayList.wrap(new String[]{}), o -> true);
        builder.pop();
        CONFIG = builder.build();
    }

    public GrassNotFloating() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG);
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> ForgeRegistries.BLOCKS.forEach(block -> {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
            if (id != null) ((Floatable)block).grassNotFloating$setShouldNotFloat(BLOCKS_THAT_SHOULD_NOT_FLOAT.get().contains(id.toString()) || BLOCKS_THAT_SHOULD_NOT_FLOAT_NAMESPACE_ONLY.get().contains(id.getNamespace()) || BLOCKS_THAT_SHOULD_NOT_FLOAT_PATH_ONLY.get().stream().anyMatch(s -> id.getPath().contains(s)));
        })));
    }
}
