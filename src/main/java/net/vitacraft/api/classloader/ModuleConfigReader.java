package net.vitacraft.api.classloader;

import net.vitacraft.api.config.ModuleConfig;
import org.simpleyaml.configuration.implementation.snakeyaml.lib.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for reading module configuration files.
 * <p>
 * This class provides a utility method for reading module configuration files
 * from JAR files and extracting the module dependencies.
 * </p>
 */
public class ModuleConfigReader {
    private static final Logger logger = LoggerFactory.getLogger("MoBot");

    /**
     * Reads the module configuration file from a JAR file and extracts the module dependencies.
     *
     * @param jarFile the JAR file containing the module configuration
     */
    public static ModuleConfig readConfig(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry("module.yml");
            if (entry == null) {
                return null;
            }

            try (InputStream input = jar.getInputStream(entry)) {
                Yaml yaml = new Yaml();
                return yaml.loadAs(input, ModuleConfig.class);
            }
        } catch (Exception e) {
            logger.error("Failed to read module.yml from {}", jarFile.getName(), e);
            return null;
        }
    }
}
