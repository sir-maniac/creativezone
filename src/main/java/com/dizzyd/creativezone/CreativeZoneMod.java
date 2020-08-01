// ***************************************************************************
//
//  Copyright 2017 David (Dizzy) Smith, dizzyd@dizzyd.com
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ***************************************************************************

package com.dizzyd.creativezone;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.JarVersionLookupHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.HashSet;

@Mod(CreativeZoneMod.MODID)
public class CreativeZoneMod {
    public static final String MODID = "creativezone";

    private static final Logger LOGGER = LogManager.getLogger();

    static int checkInterval;
    static int zoneRadius;
    static HashSet<String> whitelist = new HashSet<>();

    public CreativeZoneMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        modEventBus.addListener(this::preInit);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CreativeZoneConfig.COMMON_SPEC, MODID + ".toml");
        modEventBus.register(CreativeZoneConfig.class);
    }

    public void preInit(final FMLCommonSetupEvent e) {
        LOGGER.info("Creative Zone Mod {}",
                JarVersionLookupHandler.getImplementationVersion(CreativeZoneMod.class));
    }

    public void onServerStart(final FMLServerStartingEvent e) {
        CreativeZoneCommands.register(e.getCommandDispatcher());
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static class EventHandler {
        long lastCheck = 0;

        @SubscribeEvent
        public void onWorldTick(TickEvent.WorldTickEvent e) {
            if (!e.world.isRemote && e.phase == TickEvent.Phase.START && e.world.getDimension().isSurfaceWorld()) {
                long now = Instant.now().getEpochSecond();
                if (now - lastCheck > checkInterval) {
                    BlockPos spawn = e.world.getSpawnPoint();
                    int zoneRadiusSq = zoneRadius * zoneRadius;
                    for (ServerPlayerEntity p : e.world.getServer().getPlayerList().getPlayers()) {
                        // If the user is inside the zone radius, force them back to creative
                        if (p.getDistanceSq(spawn.getX(), p.getPosY(), spawn.getZ()) < zoneRadiusSq) {
                            p.setGameType(GameType.CREATIVE);
                        } else {
                            // Otherwise, the user is outside the radius and we need to force
                            // them back to survival (assuming they're not on the whitelist)
                            if (!whitelist.contains(p.getName().getString())) {
                                p.setGameType(GameType.SURVIVAL);
                            }
                        }
                    }
                    lastCheck = now;
                }
            }
        }
    }
}

