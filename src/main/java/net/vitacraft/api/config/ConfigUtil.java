package net.vitacraft.api.config;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The ConfigUtil class provides utility methods for loading, saving, and managing YAML configuration files.
 */
public class ConfigUtil {
    private final File file;
    private final FileConfiguration config;

    /**
     * Constructs a ConfigUtil instance with the specified configuration directory and file name.
     *
     * @param configDir      the directory where the configuration file is located.
     * @param configFileName the name of the configuration file.
     */
    public ConfigUtil(Path configDir, String configFileName) {
        this(configDir + "/" + configFileName);
    }

    /**
     * Constructs a ConfigUtil instance with the specified file path.
     *
     * @param path the path to the configuration file.
     */
    public ConfigUtil(String path) {
        this.file = new File(path);
        try {
            if (!this.file.exists()) {
                InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(this.file.getName());
                if (resourceStream != null) {
                    Files.copy(resourceStream, this.file.toPath());
                } else {
                    this.file.createNewFile();
                }
            }
            this.config = YamlConfiguration.loadConfiguration(this.file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves the configuration to the file.
     */
    public void save() {
        try {
            this.config.save(this.file);
        } catch (Exception ignored) {
        }
    }

    /**
     * Gets the configuration file.
     *
     * @return the configuration file.
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Gets the FileConfiguration object.
     *
     * @return the FileConfiguration object.
     */
    public FileConfiguration getConfig() {
        return this.config;
    }
}
