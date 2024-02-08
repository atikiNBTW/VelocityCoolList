package com.atikinbtw.velocitycoollist.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.concurrent.CompletableFuture;

public final class MainCommand {
    private static ProxyServer proxy;

    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        MainCommand.proxy = proxy;

        LiteralCommandNode<CommandSource> vclistNode =
                BrigadierCommand.literalArgumentBuilder("vclist").executes(CommandHelper::about)
                        .then(LiteralArgumentBuilder.<CommandSource>literal("status").executes(CommandHelper::status).requires(source -> source.hasPermission("vclist.admin")))

                        .then(LiteralArgumentBuilder.<CommandSource>literal("enable").requires(source -> source.hasPermission("vclist.admin")).executes(CommandHelper::enable))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("disable").requires(source -> source.hasPermission("vclist.admin")).executes(CommandHelper::disable))

                        .then(LiteralArgumentBuilder.<CommandSource>literal("add").requires(source -> source.hasPermission("vclist.manage")).executes(CommandHelper::addUser)
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.word()).executes(CommandHelper::addUser).suggests(MainCommand::suggestAll)))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("remove").requires(source -> source.hasPermission("vclist.manage")).executes(CommandHelper::removeUser)
                                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.word()).executes(CommandHelper::removeUser).suggests(MainCommand::suggestAll)))

                        .then(LiteralArgumentBuilder.<CommandSource>literal("list").requires(source -> source.hasPermission("vclist.manage")).executes(CommandHelper::list))
                        .then(LiteralArgumentBuilder.<CommandSource>literal("reload").requires(source -> source.hasPermission("vclist.admin")).executes(CommandHelper::reload))

                        .then(LiteralArgumentBuilder.<CommandSource>literal("clear").requires(source -> source.hasPermission("vclist.manage")).executes(CommandHelper::clear))

                        .build();

        // BrigadierCommand implements Command
        return new BrigadierCommand(vclistNode);
    }

    private static CompletableFuture<Suggestions> suggestAll(CommandContext<CommandSource> commandSourceCommandContext, SuggestionsBuilder builder) {
        // suggest all players
        proxy.getAllPlayers().forEach(player -> builder.suggest(
                player.getUsername(),
                // add tooltip
                VelocityBrigadierMessage.tooltip(
                        MiniMessage.miniMessage().deserialize("<rainbow>" + player.getUsername())
                )
        ));

        return builder.buildFuture();
    }
}