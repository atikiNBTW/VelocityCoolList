package com.atikinbtw.velocitycoollist.commands;

import com.atikinbtw.velocitycoollist.Config;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;

public final class CommandHelper {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static int about(CommandContext<CommandSource> context) {
        context.getSource().sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + "<rainbow>VelocityCoolList by atikiNBTW" + "\n         <dark_green>Version 2.1.0-SNAPSHOT"));

        return Command.SINGLE_SUCCESS;
    }

    public static int status(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        // get the status of the plugin from the config
        String status = (boolean) Config.get("enabled") ? Config.getMessage("whitelist_enabled") : Config.getMessage("whitelist_disabled");

        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                replaceSource(
                        replaceStatus(Config.getMessage("status"), status),
                        getSourceName(source)
                )
        ));

        return Command.SINGLE_SUCCESS;
    }

    public static int enable(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        // check if already enabled or not
        if ((boolean) Config.get("enabled")) {
            source.sendMessage(MINI_MESSAGE.deserialize(replaceSource(Config.get("prefix") + " " + Config.getMessage("already_enabled"), getSourceName(source))));
        } else {
            Config.setAndSave("enabled", true);
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("enable"), getSourceName(source))));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int disable(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        // check if already disabled or not
        if (!(boolean) Config.get("enabled")) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("already_disabled"), getSourceName(source))));
        } else {
            Config.setAndSave("enabled", false);
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("disable"), getSourceName(source))));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int addUser(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String username;

        try {
            // get the username from argument
            username = context.getArgument("username", String.class);
        } catch (Exception e) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("add_incorrect_usage"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        // check if it's already in the whitelist
        if (Config.getWhitelist().contains(username)) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                            replacePlayer(
                                    replaceSource(Config.getMessage("already_on_whitelist"), getSourceName(source)),
                                    username)
                    )
            );

            return Command.SINGLE_SUCCESS;
        }

        // add and save the file
        Config.getWhitelist().add(username);
        Config.saveWhitelistFile();
        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                        replacePlayer(
                                replaceSource(Config.getMessage("add"), getSourceName(source)),
                                username)
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int removeUser(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String username = "";

        try {
            // get the username from argument
            username = context.getArgument("username", String.class);
        } catch (Exception e) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("remove_incorrect_usage"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        // check if it's not present in the whitelist
        if (!Config.getWhitelist().contains(username)) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                            replacePlayer(
                                    replaceSource(Config.getMessage("not_on_whitelist"), getSourceName(source)),
                                    username)
                    )
            );
            return Command.SINGLE_SUCCESS;
        }

        // remove and save the file
        Config.getWhitelist().remove(username);
        Config.saveWhitelistFile();
        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                        replacePlayer(
                                replaceSource(Config.getMessage("remove"), getSourceName(source)),
                                username)
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        // check if empty and return a message
        if (Config.getWhitelist().isEmpty()) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("list_no_players"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        // return the list of players in the whitelist
        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " +
                        replaceWhitelist(
                                replaceSource(Config.getMessage("list"), getSourceName(source)), String.valueOf(Config.getWhitelist().size()), String.join(", ", Config.getWhitelist()))
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int clear(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        // check if the clear command is enabled
        if (!(boolean) Config.get("enable_clear_command")) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("clear_disabled"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        // clear the whitelist and save it
        Config.getWhitelist().clear();
        Config.saveWhitelistFile();

        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("clear"), getSourceName(source))));
        return Command.SINGLE_SUCCESS;
    }

    public static int reload(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        try {
            // reload the config
            Config.reload();
        } catch (IOException e) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("reload_error"), getSourceName(source))));
            Config.getPlugin().getLogger().error("Error happened while reloading the plugin: ", e);
            return Command.SINGLE_SUCCESS;
        }

        source.sendMessage(MINI_MESSAGE.deserialize(Config.get("prefix") + " " + replaceSource(Config.getMessage("reload"), getSourceName(source))));
        return Command.SINGLE_SUCCESS;
    }

    private static String replaceStatus(String message, String status) {
        return message
                .replace("$STATUS", status);
    }

    private static String replacePlayer(String message, String playerName) {
        return message
                .replace("$PLAYER", playerName);
    }

    private static String replaceSource(String message, String sourceName) {
        return message
                .replace("$SOURCE", sourceName);
    }

    private static String replaceWhitelist(String message, String size, String list) {
        return message
                .replace("$WHITELIST_SIZE", size)
                .replace("$WHITELIST", list);
    }

    private static String getSourceName(CommandSource source) {
        // check if it's a player or return the console name
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        } else {
            return "CONSOLE";
        }
    }
}