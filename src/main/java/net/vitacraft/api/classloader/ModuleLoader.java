package net.vitacraft.api.classloader;

import net.vitacraft.api.MBModule;
import net.vitacraft.exceptions.CircularDependencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Loads modules from JAR files in the modules directory.
 * <p>
 * This class loads modules from JAR files in the modules directory, sorts them based on dependencies, and returns a list of modules.
 * </p>
 */
public class ModuleLoader {
    private static final Logger logger = LoggerFactory.getLogger("MoBot");

    /**
     * Loads modules from JAR files in the modules directory.
     *
     * @param modulesPath the path to the modules directory
     * @return a list of {@link MBModule} instances
     */
    public static List<MBModule> loadModules(String modulesPath) {
        List<MBModule> modules = new ArrayList<>();
        File modulesDir = new File(modulesPath);
        if (modulesDir.isDirectory()) {
            File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                Map<String, MBModule> moduleMap = new HashMap<>();
                Map<String, Set<String>> dependencyGraph = new HashMap<>();
                for (File jarFile : jarFiles) {
                    try {
                        URL[] urls = new URL[]{jarFile.toURI().toURL()};
                        URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

                        ServiceLoader<MBModule> serviceLoader = ServiceLoader.load(MBModule.class, classLoader);
                        for (MBModule module : serviceLoader) {
                            String moduleName = module.getClass().getName();
                            moduleMap.put(moduleName, module);
                            dependencyGraph.putIfAbsent(moduleName, new HashSet<>());
                            ModuleConfigReader.readConfig(jarFile, moduleName, dependencyGraph);
                        }
                    } catch (Exception e) {
                        logger.error("Failed to load JAR file: {}", jarFile.getName(), e);
                    }
                }

                try {
                    List<String> sortedModuleNames = ModuleSorter.topologicalSort(dependencyGraph);
                    for (String moduleName : sortedModuleNames) {
                        modules.add(moduleMap.get(moduleName));
                    }
                } catch (CircularDependencyException e) {
                    logger.error("Failed to sort modules: {}", e.getMessage());
                }
            } else {
                logger.warn("No JAR files found in the modules directory.");
            }
        } else {
            logger.error("Modules directory is not a directory.");
        }
        return modules;
    }
}
