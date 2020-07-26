package com.dizzyd.creativezone;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class CreativeZoneCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("creativezone")
                        .requires(cs -> cs.hasPermissionLevel(3))
                        .then(RadiusCmd.register())
                        .then(WhitelistCmd.register())
        );
    }

    public static class RadiusCmd  {
        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("radius")
                    .then(Commands.argument("newRadius", IntegerArgumentType.integer(25, 1000))
                        //.requires(cs -> cs.hasPermissionLevel(2))
                        .executes(ctx -> {
                            int newRadius = IntegerArgumentType.getInteger(ctx, "newRadius");
                            // Ok, we have a properly bounded radius; update the radius and save new config
                            CreativeZoneMod.zoneRadius = newRadius;
                            CreativeZoneConfig.Common.zoneRadius.set(newRadius);
                            CreativeZoneConfig.Common.zoneRadius.save();
                            ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.radius.set", newRadius), true);
                            return 0;
                    }))
                    .executes(ctx -> {
                        int zoneRadius = CreativeZoneMod.zoneRadius;
                        // Display the current radius
                        ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.radius", zoneRadius), true);
                        return 0;
                    });
        }
    }

    public static class WhitelistCmd {
        static ArgumentBuilder<CommandSource, ?> register() {
            return Commands.literal("whitelist")
                    //.requires(cs -> cs.hasPermissionLevel(2))
                    .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> {
                                String playerName = EntityArgument.getPlayer(ctx, "player").getName().getString();
                                add(playerName);
                                ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.whitelist.added", playerName), true);
                                return 0;
                            })))
                    .then(Commands.literal("rm")
                        .then(Commands.argument("player", EntityArgument.player())
                            .executes(ctx -> {
                                String playerName = EntityArgument.getPlayer(ctx, "player").getName().getString();
                                remove(playerName);
                                ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.whitelist.removed", playerName), true);
                                return 0;
                            })))
                    .then(Commands.literal("clear")
                            .executes(ctx -> {
                                clear();
                                ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.whitelist.cleared"), true);
                                return 0;
                            }))
                    .executes(ctx -> {
                        // No arguments provided; display all whitelisted users
                        StringBuilder b = new StringBuilder();
                        for (String k: CreativeZoneMod.whitelist) {
                            b.append("\n* ").append(k);
                        }
                        ctx.getSource().sendFeedback(new TranslationTextComponent("creativezone.whitelist", b.toString()), true);
                        return 0;
                    });
        }

        @SuppressWarnings("unchecked")
        private static void add(String name) {
            if (!CreativeZoneMod.whitelist.contains(name)) {
                CreativeZoneMod.whitelist.add(name);
                ((List<String>)CreativeZoneConfig.Common.whitelist.get()).add(name);
                CreativeZoneConfig.Common.whitelist.save();
            }
        }

        private static void remove(String name) {
            if (CreativeZoneMod.whitelist.contains(name)) {
                CreativeZoneMod.whitelist.remove(name);
                CreativeZoneConfig.Common.whitelist.get().remove(name);
                CreativeZoneConfig.Common.whitelist.save();
            }
        }

        private static void clear() {
            CreativeZoneMod.whitelist.clear();
            CreativeZoneConfig.Common.whitelist.get().clear();
            CreativeZoneConfig.Common.whitelist.save();
        }

    }
}
