package com.atikinbtw.velocitycoollist.commands;

import com.atikinbtw.velocitycoollist.Config;
import com.atikinbtw.velocitycoollist.VelocityCoolList;
import com.atikinbtw.velocitycoollist.Whitelist;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class CommandHelper {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static int about(CommandContext<CommandSource> context) {
        context.getSource().sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + "VelocityCoolList v" + VelocityCoolList.VERSION));

        return Command.SINGLE_SUCCESS;
    }

    public static int status(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        String status = Config.getInstance().getBoolean("enabled") ? Config.getInstance().getMessage("whitelist_enabled") : Config.getInstance().getMessage("whitelist_disabled");

        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                replaceSource(
                        replaceStatus(Config.getInstance().getMessage("status"), status),
                        getSourceName(source)
                )
        ));

        return Command.SINGLE_SUCCESS;
    }

    public static int enable(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        if (Config.getInstance().getBoolean("enabled")) {
            source.sendMessage(MINI_MESSAGE.deserialize(replaceSource(Config.getInstance().getString("prefix") + " " + Config.getInstance().getMessage("already_enabled"), getSourceName(source))));
        } else {
            Config.getInstance().setAndSave("enabled", true);
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("enable"), getSourceName(source))));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int disable(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        if (!(boolean) Config.getInstance().getBoolean("enabled")) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("already_disabled"), getSourceName(source))));
        } else {
            Config.getInstance().setAndSave("enabled", false);
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("disable"), getSourceName(source))));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int addUser(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String username;

        try {
            username = context.getArgument("username", String.class);
        } catch (Exception e) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("add_incorrect_usage"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        if (Whitelist.getInstance().contains(username)) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                            replacePlayer(
                                    replaceSource(Config.getInstance().getMessage("already_on_whitelist"), getSourceName(source)),
                                    username)
                    )
            );

            return Command.SINGLE_SUCCESS;
        }

        // add and save the file
        Whitelist.getInstance().addPlayer(username);
        Whitelist.getInstance().saveFile();
        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                        replacePlayer(
                                replaceSource(Config.getInstance().getMessage("add"), getSourceName(source)),
                                username)
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int removeUser(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();
        String username;

        try {
            username = context.getArgument("username", String.class);
        } catch (Exception e) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("remove_incorrect_usage"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        if (!Whitelist.getInstance().contains(username)) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                            replacePlayer(
                                    replaceSource(Config.getInstance().getMessage("not_on_whitelist"), getSourceName(source)),
                                    username)
                    )
            );
            return Command.SINGLE_SUCCESS;
        }

        // remove and save the file
        Whitelist.getInstance().removePlayer(username);
        Whitelist.getInstance().saveFile();
        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                        replacePlayer(
                                replaceSource(Config.getInstance().getMessage("remove"), getSourceName(source)),
                                username)
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int list(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        if (Whitelist.getInstance().isWhitelistEmpty()) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("list_no_players"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " +
                        replaceWhitelist(
                                replaceSource(Config.getInstance().getMessage("list"),
                                        getSourceName(source)), String.valueOf(Whitelist.getInstance().getWhitelist().size()),
                                String.join(", ", Whitelist.getInstance().getWhitelist())
                        )
                )
        );

        return Command.SINGLE_SUCCESS;
    }

    public static int clear(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        if (!Config.getInstance().getBoolean("enable_clear_command")) {
            source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("clear_disabled"), getSourceName(source))));
            return Command.SINGLE_SUCCESS;
        }

        Whitelist.getInstance().clear();
        Whitelist.getInstance().saveFile();

        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("clear"), getSourceName(source))));
        return Command.SINGLE_SUCCESS;
    }

    public static int reload(CommandContext<CommandSource> context) {
        CommandSource source = context.getSource();

        Config.getInstance().reload();
        Whitelist.getInstance().reload();

        source.sendMessage(MINI_MESSAGE.deserialize(Config.getInstance().getString("prefix") + " " + replaceSource(Config.getInstance().getMessage("reload"), getSourceName(source))));
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
        if (source instanceof Player) {
            return ((Player) source).getUsername();
        } else {
            return "CONSOLE";
        }
    }
}