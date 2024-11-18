package net.vitacraft.api.classloader;

import net.vitacraft.api.config.ConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
     * @param moduleName the name of the module
     * @param dependencyGraph the dependency graph to populate with module dependencies
     */
    public static void readConfig(File jarFile, String moduleName, Map<String, Set<String>> dependencyGraph) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry entry = jar.getJarEntry("module.yml");
            if (entry != null) {
                try (InputStream inputStream = jar.getInputStream(entry)) {
                    ConfigLoader configLoader = new ConfigLoader(inputStream);
                    List<String> dependencies = configLoader.getConfig().getStringList("dependencies");
                    if (dependencies != null) {
                        dependencyGraph.put(moduleName, new HashSet<>(dependencies));
                    }
                }
            } else {
                logger.warn("Module configuration not found for module: {}", moduleName);
            }
        } catch (Exception e) {
            logger.error("Error reading module configuration for module: {}", moduleName, e);
        }
    }
}
