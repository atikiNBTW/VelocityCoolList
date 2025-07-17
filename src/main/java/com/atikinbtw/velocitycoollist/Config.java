package com.atikinbtw.velocitycoollist;

import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Config {
    private static Config INSTANCE;
    private final VelocityCoolList plugin;
    private YamlFile config;
    private YamlFile messages;

    public Config(VelocityCoolList plugin) {
        this.plugin = plugin;
        INSTANCE = this;
    }

    public static Config getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Config has not been initialized");

        return INSTANCE;
    }

    public void saveConfigFile() {
        plugin.scheduleTask(() -> {
            try {
                config.save();
            } catch (IOException e) {
                VelocityCoolList.LOGGER.error("Error happened while saving the config.yml: ", e);
            }
        });
    }

    public void reload() {
        try {
            messages.loadWithComments();
            config.loadWithComments();
        } catch (IOException e) {
            VelocityCoolList.LOGGER.error("Error happened while reloading: ", e);
        }
    }

    public void setAndSave(String key, Object value) {
        config.set(key, value);

        saveConfigFile();
    }

    public void setMessage(String path, String value) {
        messages.set(path, value);
    }

    public void saveMessages() {
        plugin.scheduleTask(() -> {
            try {
                messages.save();
            } catch (IOException e) {
                VelocityCoolList.LOGGER.error("Error happened while saving the config.yml: ", e);
            }
        });
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public Boolean getBoolean(String key) {
        return config.getBoolean(key);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return config.getBoolean(key, defaultValue);
    }

    public int getInt(String key) {
        return config.getInt(key);
    }

    public String getMessage(String key) {
        return messages.getString(key);
    }

    public void init() {
        VelocityCoolList.LOGGER.info("Loading config...");

        config = new YamlFile(Path.of(plugin.DATADIRECTORY + "/config.yml").toUri());
        messages = new YamlFile(Path.of(plugin.DATADIRECTORY + "/messages.yml").toUri());

        if (!plugin.DATADIRECTORY.toFile().exists()) {
            try {
                plugin.DATADIRECTORY.toFile().mkdir();
            } catch (Exception e) {
                VelocityCoolList.LOGGER.error("Failed to create plugin data directory: ", e);
                return;
            }
        }

        if (!config.exists()) {
            try (InputStream resource = VelocityCoolList.class.getResourceAsStream("/config.yml");) {
                Files.copy(resource, Path.of(config.getFilePath()));
            } catch (Exception e) {
                VelocityCoolList.LOGGER.error("Error happened while creating config.yml: ", e);
                return;
            }
        }

        if (!messages.exists()) {
            try (InputStream resource = VelocityCoolList.class.getResourceAsStream("/messages.yml")) {
                Files.copy(resource, Path.of(messages.getFilePath()));
            } catch (Exception e) {
                VelocityCoolList.LOGGER.error("Error happened while creating messages.yml: ", e);
                return;
            }
        }

        try {
            config.loadWithComments();
        } catch (IOException e) {
            VelocityCoolList.LOGGER.error("Error happened while loading config.yml: ", e);
            return;
        }

        try {
            messages.loadWithComments();
        } catch (IOException e) {
            VelocityCoolList.LOGGER.error("Error happened while loading messages.yml: ", e);
            return;
        }

        new Migration(plugin, config).migrateIfNeeded();
    }
}