package me.kall.grassnotfloating.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.kall.grassnotfloating.GrassNotFloating;
import me.kall.grassnotfloating.ext.Trackable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FloatConfig {
    public static final ForgeConfigSpec INSTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TRACKED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push(GrassNotFloating.MOD_ID);
        TRACKED = builder.defineList("UnfloatableBlocks", Lists.newArrayList("minecraft:grass", "minecraft:fern", "minecraft:tall_grass"), Predicates.alwaysTrue());
        builder.pop();
        INSTANCE = builder.build();
    }

    public static void initConfig() {
        Set<String> tracked = new ObjectOpenHashSet<>(TRACKED.get());
        for (Map.Entry<ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ((Trackable)entry.getValue()).float$setTracked(tracked.contains(entry.getKey().location().toString()));
        }
    }

    public static void configLoad(@NotNull ModConfigEvent.Reloading event) {
        if (event.getConfig().getModId().equals(GrassNotFloating.MOD_ID)) initConfig();
    }
}
