package com.atikinbtw.velocitycoollist;

import com.google.gson.Gson;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Whitelist {
    private static Whitelist INSTANCE;
    private final VelocityCoolList plugin;
    private final Path whitelistPath;
    @Getter
    private List<String> whitelist = List.of();

    public Whitelist(VelocityCoolList plugin) {
        this.plugin = plugin;
        this.whitelistPath = Path.of(plugin.DATADIRECTORY + "/whitelist.json");
        INSTANCE = this;
    }

    public static Whitelist getInstance() {
        if (INSTANCE == null)
            throw new IllegalStateException("Config has not been initialized");

        return INSTANCE;
    }

    public void reload() {
        loadWhitelist();
    }

    private void loadWhitelist() {
        Gson gson = new Gson();
        try {
            if (!Files.exists(whitelistPath)) {
                Files.createFile(whitelistPath);
                Files.writeString(whitelistPath, "[]");
            }

            try (Reader reader = new InputStreamReader(whitelistPath.toUri().toURL().openStream())) {
                whitelist = gson.fromJson(reader, List.class);
            }
        } catch (IOException e) {
            VelocityCoolList.LOGGER.error("Error happened while loading whitelist: ", e);
        }
    }

    public Boolean isWhitelistEmpty() {
        return whitelist.isEmpty();
    }

    public void clear() {
        this.whitelist.clear();
    }

    public void removePlayer(String nickname) {
        this.whitelist.remove(nickname);
    }

    public void addPlayer(String nickname) {
        this.whitelist.add(nickname);
    }

    public Boolean contains(String nickname) {
        return whitelist.contains(nickname);
    }

    public void saveFile() {
        plugin.scheduleTask(() -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(whitelist);

                Files.writeString(Path.of(plugin.DATADIRECTORY + "/whitelist.json"), json);
            } catch (IOException e) {
                VelocityCoolList.LOGGER.error("Error happened while saving whitelist.json: ", e);
            }
        });
    }

    public void init() {
        VelocityCoolList.LOGGER.info("Loading whitelist...");

        if (!plugin.DATADIRECTORY.toFile().exists()) {
            try {
                plugin.DATADIRECTORY.toFile().mkdir();
            } catch (Exception e) {
                VelocityCoolList.LOGGER.error("Failed to create plugin data directory: ", e);
                return;
            }
        }

        loadWhitelist();

    }
}