package com.atikinbtw.velocitycoollist;

import com.google.gson.Gson;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public final class Config {
    private static Config INSTANCE;
    private final VelocityCoolList plugin;
    private YamlFile config;
    private YamlFile messages;
    private HashSet<String> whitelist = new HashSet<>();

    public Config(VelocityCoolList plugin) {
        this.plugin = plugin;
        INSTANCE = this;
    }

    public static Config getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Config has not been initialized");

        return INSTANCE;
    }

    public void saveWhitelistFile() {
        plugin.scheduleTask(() -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(whitelist);

                Files.writeString(Path.of(plugin.DATADIRECTORY + "/whitelist.json"), json);
            } catch (IOException e) {
                plugin.LOGGER.error("Error happened while saving whitelist.json: ", e);
            }
        });
    }

    public void saveConfigFile() {
        plugin.scheduleTask(() -> {
            try {
                config.save();
            } catch (IOException e) {
                plugin.LOGGER.error("Error happened while saving the config.yml: ", e);
            }
        });
    }

    public void reload() throws IOException {
        loadWhitelist();
        messages.loadWithComments();
        config.loadWithComments();
    }

    public void setAndSave(String key, Object value) {
        config.set(key, value);

        saveConfigFile();
    }

    public void setConfig(String path, Object value) {
        config.set(path, value);
    }

    public void setMessage(String path, String value) {
        messages.set(path, value);
    }

    public void saveMessages() {
        plugin.scheduleTask(() -> {
            try {
                messages.save();
            } catch (IOException e) {
                plugin.LOGGER.error("Error happened while saving the config.yml: ", e);
            }
        });
    }

    public Object get(String key) {
        return config.get(key);
    }

    public Boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public void addWhitelist(String nickname) {
        this.whitelist.add(nickname);
    }

    public Boolean isWhitelistEmpty() {
        return whitelist.isEmpty();
    }

    public void clearWhitelist() {
        this.whitelist.clear();
    }

    public void removeWhitelist(String nickname) {
        this.whitelist.remove(nickname);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public Boolean whitelistContains(String nickname) {
        return whitelist.contains(nickname);
    }

    public Object get(String key, Object defaultValue) {
        return config.get(key, defaultValue);
    }

    public String getMessage(String key) {
        return messages.getString(key);
    }

    private void loadWhitelist() throws IOException {
        Path whitelistPath = Path.of(plugin.DATADIRECTORY + "/whitelist.json");
        Gson gson = new Gson();

        if (!Files.exists(whitelistPath)) {
            Files.createFile(whitelistPath);
            Files.writeString(whitelistPath, "[]");
        }

        Reader reader = new InputStreamReader(whitelistPath.toFile().toURI().toURL().openStream());
        whitelist = gson.fromJson(reader, HashSet.class);
        reader.close();
    }

    public void initialize() {
        plugin.LOGGER.info("Loading config...");

        config = new YamlFile(Path.of(plugin.DATADIRECTORY + "/config.yml").toUri());
        messages = new YamlFile(Path.of(plugin.DATADIRECTORY + "/messages.yml").toUri());

        if (!plugin.DATADIRECTORY.toFile().exists()) {
            try {
                plugin.DATADIRECTORY.toFile().mkdir();
            } catch (Exception e) {
                plugin.LOGGER.error("Failed to create plugin data directory: ", e);
                return;
            }
        }

        try {
            loadWhitelist();
        } catch (IOException e) {
            plugin.LOGGER.error("Error happened while loading whitelist.json: ", e);
        }

        if (!config.exists()) {
            try {
                InputStream resource = VelocityCoolList.class.getResourceAsStream("/config.yml");

                Files.copy(resource, Path.of(config.getFilePath()));
                resource.close();
            } catch (Exception e) {
                plugin.LOGGER.error("Error happened while creating config.yml: ", e);
                return;
            }
        }

        if (!messages.exists()) {
            try {
                InputStream resource = VelocityCoolList.class.getResourceAsStream("/messages.yml");

                Files.copy(resource, Path.of(messages.getFilePath()));
                resource.close();
            } catch (Exception e) {
                plugin.LOGGER.error("Error happened while creating messages.yml: ", e);
                return;
            }
        }

        try {
            config.loadWithComments();
        } catch (IOException e) {
            plugin.LOGGER.error("Error happened while loading config.yml: ", e);
            return;
        }

        try {
            messages.loadWithComments();
        } catch (IOException e) {
            plugin.LOGGER.error("Error happened while loading messages.yml: ", e);
            return;
        }

        if (Path.of(plugin.DATADIRECTORY + "/config.toml").toFile().exists()) {
            new Migration(messages, plugin, this).migrateOldTomlConfig();
        }
    }
}