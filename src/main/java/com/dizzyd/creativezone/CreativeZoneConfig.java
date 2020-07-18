package com.dizzyd.creativezone;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class CreativeZoneConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig Common;
    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        Common = specPair.getLeft();
    }

    public static class CommonConfig {
        final ForgeConfigSpec.IntValue scanInterval;
        final ForgeConfigSpec.IntValue zoneRadius;
        final ForgeConfigSpec.ConfigValue<List<? extends String>> whitelist;


        CommonConfig(final ForgeConfigSpec.Builder builder) {
            builder.push("config");
            scanInterval = builder
                    .comment("Sets the interval (in seconds) for scanning player locations")
                    .defineInRange("ScanInterval", 1, 1, 60);
            zoneRadius = builder
                    .comment("Sets the radius of the creative zone")
                    .defineInRange("ZoneRadius", 25, 5, 1000);
            whitelist = builder
                        .comment("Gets the list of whitelisted users")
                        .defineList("Whitelist", ArrayList::new, name -> ((String)name).length() > 0);

        }
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            LOGGER.debug("Loaded Creative Zone config file {}", configEvent.getConfig().getFileName());
            bake();
        }
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            LOGGER.debug("Creative Zone config just got changed on the file system!");
            bake();
        }
    }

    public static void bake() {
        CreativeZoneMod.whitelist.clear();
        CreativeZoneMod.whitelist.addAll(Common.whitelist.get());
        CreativeZoneMod.checkInterval = Common.scanInterval.get();
        CreativeZoneMod.zoneRadius = Common.zoneRadius.get();
    }
}
