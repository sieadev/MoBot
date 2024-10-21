package net.vitacraft.api.config;

import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ModuleConfigLoader {
    private final File file;
    private final FileConfiguration config;

    /**
     * Constructs a ConfigLoader instance that loads a configuration file from within a JAR.
     *
     * @param moduleClass the class from the module JAR, used to load resources.
     * @param resourceName the name of the resource file (e.g., "module.yml").
     */
    public ModuleConfigLoader(Class<?> moduleClass, String resourceName) {
        this.file = null; // No file associated when loading from a resource within the JAR
        try (InputStream resourceStream = moduleClass.getClassLoader().getResourceAsStream(resourceName)) {
            if (resourceStream == null) {
                throw new IOException("Resource " + resourceName + " not found in the JAR.");
            }
            this.config = YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource " + resourceName + " from the JAR.", e);
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
