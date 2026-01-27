package dev.mariany.copperworks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.mariany.copperworks.Copperworks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigHandler<T> {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final File file;
    protected final T defaultConfig;
    protected T config;

    public ConfigHandler(String id, T defaultConfig) {
        this.defaultConfig = defaultConfig;
        this.config = defaultConfig;
        this.file = new File("config/" + id + ".json5");
    }

    public T getConfig() {
        return this.config;
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        if (this.file.exists()) {
            try (FileReader reader = new FileReader(this.file)) {
                this.config = (T) GSON.fromJson(reader, this.config.getClass());
            } catch (IOException error) {
                this.config = this.defaultConfig;
                Copperworks.LOGGER.error("Failed to load config: {}", error.getMessage());
            }
        }

        this.saveConfig();
    }

    protected void saveConfig() {
        try {
            if (this.file.getParentFile().mkdirs()) {
                Copperworks.LOGGER.info("Creating parent directory for config");
            }

            try (FileWriter writer = new FileWriter(this.file)) {
                GSON.toJson(this.config, writer);
            }
        } catch (IOException error) {
            Copperworks.LOGGER.error("Failed to save config: {}", error.getMessage());
        }
    }
}

