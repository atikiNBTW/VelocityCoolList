package com.atikinbtw.velocitycoollist;

import com.atikinbtw.velocitycoollist.commands.MainCommand;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;
import java.util.Scanner;

@Plugin(
        id = "velocitycoollist",
        name = "VelocityCoolList",
        version = "2.0.0-SNAPSHOT",
        description = "Plugin for Velocity that provides nickname-based whitelisting without complex!",
        url = "https://modrinth.com/plugin/velocitycoollist",
        authors = {"atikiNBTW"}
)
public class VelocityCoolList {

    @Inject
    @Getter
    private final Logger logger;
    private final ProxyServer proxy;
    @Getter
    private final Path dataDirectory;
    private final String version = "2.0.0";

    @Inject
    public VelocityCoolList(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Date startTime = new Date(System.currentTimeMillis());
        // init config
        Config.initializeConfig(this);

        // register commands
        CommandManager commandManager = proxy.getCommandManager();

        CommandMeta commandMeta = commandManager.metaBuilder("vclist")
                .aliases("vcl", "velocitycoollist")
                .plugin(this)
                .build();

        BrigadierCommand commandToRegister = MainCommand.createBrigadierCommand(proxy);
        commandManager.register(commandMeta, commandToRegister);

        logger.info("VelocityCoolList has been enabled!" + " Took " + (System.currentTimeMillis() - startTime.getTime()) + "ms");
        if ((boolean) Config.get("autoupdate", true)) {
            checkForUpdates();
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    private EventTask onPlayerJoin(ServerPreConnectEvent event) {
        if (!(boolean) Config.get("enabled")) return null;
        Runnable task = () -> {
            Player player = event.getPlayer();

            // check for whitelist pass
            if (player.hasPermission("vclist.bypass") || Config.getWhitelist().contains(player.getUsername())) return;

            // disconnect and deny if not whitelisted
            player.disconnect(MiniMessage.miniMessage().deserialize(Config.getMessage("kick_message")).asComponent());
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        };

        return EventTask.async(task);
    }

    private void checkForUpdates() {
        Runnable task = (() -> {
            // define the api url
            String apiURL = "https://api.modrinth.com/v2/project/velocitycoollist/version?featured=true";

            try {
                // fetch a url to InputStream
                URL url = URI.create(apiURL).toURL();
                URLConnection conn = url.openConnection();
                InputStream is = conn.getInputStream();

                // read InputStream
                String text = "";
                try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                    text = scanner.useDelimiter("\\A").next();
                }

                if (text.isEmpty()) {
                    logger.error("Error happened while getting the latest plugin version");
                    return;
                }

                // make version and url strings
                String version = text.split("\"version_number\":\"")[1].split("\"")[0];
                String newVerUrl = text.split("\"url\":\"")[1].split("\"")[0];

                // notify the user about the new version
                if (!version.equals(this.version)) {
                    logger.info("New version of the plugin is available, please update! Url to the new version: " + newVerUrl);
                }
            } catch (IOException e) {
                logger.error("Error happened while getting the latest plugin version: ", e);
            }
        });

        scheduleTask(task);
    }

    /**
     * Schedule a task to be run.
     *
     * @param runnable the task to be scheduled
     */
    public void scheduleTask(Runnable runnable) {
        proxy.getScheduler().buildTask(this, runnable).schedule();
    }
}