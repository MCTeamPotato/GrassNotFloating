package me.kall.grassnotfloating.common.config;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Config {
    public static final ForgeConfigSpec INSTANCE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UNFLOATABLE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("GrassNotFloating");
        UNFLOATABLE = builder.defineList("UnfloatablePlants", Lists.newArrayList("minecraft:grass"), Predicates.alwaysTrue());
        builder.pop();
        INSTANCE = builder.build();
    }
}
