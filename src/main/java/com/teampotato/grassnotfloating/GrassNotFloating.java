package com.teampotato.grassnotfloating;

import com.teampotato.grassnotfloating.api.Floatable;
import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;

public class GrassNotFloating implements ModInitializer {

	@Override
	public void onInitialize() {
		ForgeConfigRegistry.INSTANCE.register("grassnotfloating", ModConfig.Type.COMMON, CONFIG);
		for (Block block : Registries.BLOCK) {
			Identifier id = Registries.BLOCK.getId(block);
			((Floatable)block).grassNotFloating$setShouldNotFloat(BLOCKS_THAT_SHOULD_NOT_FLOAT.get().contains(id.toString()) || BLOCKS_THAT_SHOULD_NOT_FLOAT_NAMESPACE_ONLY.get().contains(id.getNamespace()) || BLOCKS_THAT_SHOULD_NOT_FLOAT_PATH_ONLY.get().stream().anyMatch(s -> id.getPath().contains(s)));
		}
	}


	public static final BlockState AIR = Blocks.AIR.getDefaultState();

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
}
