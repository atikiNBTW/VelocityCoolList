package com.atikinbtw.velocitycoollist;

import com.moandjiezana.toml.Toml;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Path;

public class Migration {
    private final Config config;
    private final VelocityCoolList plugin;

    public Migration(YamlFile config, VelocityCoolList plugin, Config cfg) {
        this.config = cfg;
        this.plugin = plugin;
    }

    public void migrateOldTomlConfig() {
        plugin.scheduleTask(() -> {
            plugin.LOGGER.info("Found the old config, migrating to the new one...");
            Path oldConfigPath = Path.of(plugin.DATADIRECTORY + "/config.toml");

            Toml toml = new Toml().read(oldConfigPath.toFile());

            if (toml.getBoolean("enabled") == null || toml.getString("message") == null || toml.getString("prefix") == null) {
                plugin.LOGGER.info("The old config is missing something, skipping migration...");
                return;
            }

            config.setConfig("enabled", toml.getBoolean("enabled"));
            config.setMessage("kick_message", toml.getString("message"));
            config.setConfig("prefix", toml.getString("prefix"));

            config.saveConfigFile();
            config.saveMessages();

            oldConfigPath.toFile().delete();

            try {
                config.reload();
            } catch (IOException e) {
                plugin.LOGGER.error("Error happened while migrating the old config to the new one: ", e);
                return;
            }

            plugin.LOGGER.info("Migration completed!");
        });
    }
}