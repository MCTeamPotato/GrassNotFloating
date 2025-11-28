package me.kall.grassnotfloating.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import me.kall.grassnotfloating.GrassNotFloating;
import me.kall.grassnotfloating.ext.Trackable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FloatConfig {
    public static final ForgeConfigSpec INSTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRACKED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push(GrassNotFloating.MOD_ID);
        TRACKED = builder.comment("Support registry entries and tags", "Please add 'tag:' before tag or the parsing will go wrong", "'minecraft' namespace is not omittable").defineList("UnfloatableBlocks", Lists.newArrayList("minecraft:grass", "minecraft:fern", "minecraft:tall_grass", "tag:minecraft:flowers", "tag:minecraft:tall_flowers"), Predicates.alwaysTrue());
        builder.pop();
        INSTANCE = builder.build();
    }

    public static void initConfig() {
        Set<String> tracked = TRACKED.get().stream().filter(id -> !id.startsWith("tag:")).collect(Collectors.toSet());
        Set<ResourceLocation> tags = TRACKED.get().stream().filter(id -> id.startsWith("tag:")).map(id -> {
            try {
                String[] parts = id.split(":");
                return new ResourceLocation(parts[1], parts[2]);
            } catch (Exception exception) {
                throw new RuntimeException("Invalid entry in GrassNotFloating config: " + id);
            }
        }).collect(Collectors.toSet());
        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            String name = entry.getKey().location().toString();
            Block block = entry.getValue();
            ((Trackable)block).float$setTracked(tracked.contains(name) || block.getTags().stream().anyMatch(tags::contains));
        }
    }

    public static void configLoad(@NotNull ModConfig.Reloading event) {
        if (event.getConfig().getModId().equals(GrassNotFloating.MOD_ID)) initConfig();
    }
}
