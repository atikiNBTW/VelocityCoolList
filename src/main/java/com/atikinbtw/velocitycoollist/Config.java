package com.atikinbtw.velocitycoollist;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import lombok.Getter;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public final class Config {
    private static YamlFile config;
    private static YamlFile messages;
    @Getter
    private static HashSet<String> whitelist = new HashSet<>();
    @Getter
    private static VelocityCoolList plugin;

    /**
     * Initialize the configuration for the VelocityCoolList plugin.
     *
     * @param plugin the VelocityCoolList plugin to initialize the configuration for
     */
    public static void initializeConfig(VelocityCoolList plugin) {
        Config.plugin = plugin;
        plugin.getLogger().info("Loading config...");

        // get the files
        config = new YamlFile(Path.of(plugin.getDataDirectory() + "/config.yml").toUri());
        messages = new YamlFile(Path.of(plugin.getDataDirectory() + "/messages.yml").toUri());

        // create plugin folder if it doesn't exist
        if (!plugin.getDataDirectory().toFile().exists()) {
            try {
                plugin.getDataDirectory().toFile().mkdir();
            } catch (Exception e) {
                plugin.getLogger().error("Failed to create plugin data directory: ", e);
                return;
            }
        }

        try {
            loadWhitelist();
        } catch (IOException e) {
            plugin.getLogger().error("Error happened while loading whitelist.json: ", e);
        }

        // copy config if it doesn't exist
        if (!config.exists()) {
            try {
                InputStream resource = VelocityCoolList.class.getResourceAsStream("/config.yml");

                Files.copy(resource, Path.of(config.getFilePath()));
                resource.close();
            } catch (Exception e) {
                plugin.getLogger().error("Error happened while creating config.yml: ", e);
                return;
            }
        }

        // copy messages.yml if it doesn't exist
        if (!messages.exists()) {
            try {
                InputStream resource = VelocityCoolList.class.getResourceAsStream("/messages.yml");

                Files.copy(resource, Path.of(messages.getFilePath()));
                resource.close();
            } catch (Exception e) {
                plugin.getLogger().error("Error happened while creating messages.yml: ", e);
                return;
            }
        }

        try {
            config.loadWithComments();
        } catch (IOException e) {
            plugin.getLogger().error("Error happened while loading config.yml: ", e);
            return;
        }

        try {
            messages.loadWithComments();
        } catch (IOException e) {
            plugin.getLogger().error("Error happened while loading messages.yml: ", e);
            return;
        }

        // check if the config from previous version needs migration
        if (Path.of(plugin.getDataDirectory() + "/config.toml").toFile().exists()) {
            migrateTomlConfig();
        }
    }

    private static void loadWhitelist() throws IOException {
        Path whitelistPath = Path.of(plugin.getDataDirectory() + "/whitelist.json");
        // create Gson instance
        Gson gson = new Gson();

        // create json file if it doesn't exist and append an empty array
        if (!Files.exists(whitelistPath)) {
            Files.createFile(whitelistPath);
            Files.writeString(whitelistPath, "[]");
        }

        // read json file
        Reader reader = new InputStreamReader(whitelistPath.toFile().toURI().toURL().openStream());
        whitelist = gson.fromJson(reader, HashSet.class);
        reader.close();
    }

    public static void saveWhitelistFile() {
        plugin.scheduleTask(() -> {
            try {
                Gson gson = new Gson();
                // create json string
                String json = gson.toJson(whitelist);

                // write json file
                Files.writeString(Path.of(plugin.getDataDirectory() + "/whitelist.json"), json);
            } catch (IOException e) {
                plugin.getLogger().error("Error happened while saving whitelist.json: ", e);
            }
        });
    }

    private static void saveConfigFile() {
        plugin.scheduleTask(() -> {
            try {
                config.save();
            } catch (IOException e) {
                plugin.getLogger().error("Error happened while saving the config.yml: ", e);
            }
        });
    }

    public static void reload() throws IOException {
        loadWhitelist();
        messages.loadWithComments();
        config.loadWithComments();
    }

    public static void setAndSave(String key, Object value) {
        config.set(key, value);

        saveConfigFile();
    }

    public static Object get(String key) {
        return config.get(key);
    }

    public static Object get(String key, Object def) {
        return config.get(key, def);
    }

    public static String getMessage(String key) {
        return messages.getString(key);
    }

    private static void migrateTomlConfig() {
        plugin.scheduleTask(() -> {
            plugin.getLogger().info("Found the old config, migrating to the new one...");
            Path oldConfigPath = Path.of(plugin.getDataDirectory() + "/config.toml");

            // create toml instance
            Toml toml = new Toml().read(oldConfigPath.toFile());

            // validate the config
            if (toml.getBoolean("enabled") == null || toml.getString("message") == null || toml.getString("prefix") == null) {
                plugin.getLogger().info("The old config is missing something, skipping migration...");
                return;
            }

            // copy values
            config.set("enabled", toml.getBoolean("enabled"));
            messages.set("kick_message", toml.getString("message"));
            config.set("prefix", toml.getString("prefix"));

            // save files
            saveConfigFile();
            try {
                messages.save();
            } catch (IOException e) {
                plugin.getLogger().error("Error happened while saving the messages.yml: ", e);
                return;
            }

            // delete old config file
            oldConfigPath.toFile().delete();

            // reload plugin and clear the toml table
            try {
                reload();
            } catch (IOException e) {
                plugin.getLogger().error("Error happened while migrating the old config to the new one: ", e);
                return;
            }

            plugin.getLogger().info("Migration completed!");
        });
    }
}