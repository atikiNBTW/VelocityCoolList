package com.atikinbtw.velocitycoollist;

import com.moandjiezana.toml.Toml;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Migration {
    private final Config config;
    private final VelocityCoolList plugin;
    private final YamlFile configFile;

    public Migration(VelocityCoolList plugin, YamlFile config) {
        this.config = Config.getInstance();
        this.plugin = plugin;
        this.configFile = config;
    }

    public void migrateIfNeeded() {
        if (Path.of(plugin.DATADIRECTORY + "/config.toml").toFile().exists()) {
            migrateOldTomlConfig();
            return;
        }

        switch (config.getInt("version")) {
            case 1 -> firstToSecondVerMigration();
            case 2 -> {
            }
            default -> VelocityCoolList.LOGGER.error("Unknown config version: {}", config.getInt("version"));
        }
    }

    private void firstToSecondVerMigration() {
        plugin.scheduleTask(() -> {
            boolean enabled;
            String prefix;
            boolean enableClearCommand;

            enabled = config.getBoolean("enabled");
            prefix = config.getString("prefix");
            enableClearCommand = config.getBoolean("enable_clear_command");

            try (InputStream resource = VelocityCoolList.class.getResourceAsStream("/config.yml");) {
                Files.copy(resource, Path.of(configFile.getFilePath()));
            } catch (Exception e) {
                VelocityCoolList.LOGGER.error("Error happened while creating config.yml: ", e);
                return;
            }

            config.reload();

            configFile.set("enabled", enabled);
            configFile.set("prefix", prefix);
            configFile.set("enable_clear_command", enableClearCommand);

            config.saveConfigFile();

            config.reload();
        });
    }

    public void migrateOldTomlConfig() {
        plugin.scheduleTask(() -> {
            VelocityCoolList.LOGGER.info("Found the old config, migrating to the new one...");
            Path oldConfigPath = Path.of(plugin.DATADIRECTORY + "/config.toml");

            Toml toml;
            try {
                toml = new Toml().read(oldConfigPath.toFile());
            } catch (IllegalStateException e) {
                VelocityCoolList.LOGGER.error("The old config is broken, can't migrate automatically");
                return;
            }

            if (toml.getBoolean("enabled") == null || toml.getString("message") == null || toml.getString("prefix") == null) {
                VelocityCoolList.LOGGER.error("The old config is broken, can't migrate automatically");
                return;
            }

            configFile.set("enabled", toml.getBoolean("enabled"));
            config.setMessage("kick_message", toml.getString("message"));
            configFile.set("prefix", toml.getString("prefix"));

            config.saveConfigFile();
            config.saveMessages();

            oldConfigPath.toFile().delete();

            config.reload();

            VelocityCoolList.LOGGER.info("Migration completed!");
        });
    }
}