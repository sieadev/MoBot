package net.vitacraft.api.classloader;

import net.vitacraft.api.MBModule;
import net.vitacraft.exceptions.CircularDependencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility class for loading modules from JAR files.
 * <p>
 * This class provides a utility method for loading modules from JAR files
 * in a specified directory. The modules are loaded using a URLClassLoader
 * and are sorted based on their dependencies.
 * </p>
 */
public class ModuleLoader {
    private static final Logger logger = LoggerFactory.getLogger("MoBot");

    /**
     * Loads modules from JAR files in the specified directory.
     *
     * @param modulesPath the path to the directory containing the module JAR files
     * @return a list of loaded modules sorted based on their dependencies
     */
    public static List<MBModule> loadModules(String modulesPath) {
        File modulesDir = new File(modulesPath);
        if (!modulesDir.isDirectory()) {
            logger.error("Modules directory is not a directory.");
            return Collections.emptyList();
        }

        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            logger.warn("No JAR files found in the modules directory.");
            return Collections.emptyList();
        }

        Map<String, MBModule> moduleMap = new HashMap<>();
        Map<String, Set<String>> dependencyGraph = new HashMap<>();

        for (File jarFile : jarFiles) {
            try {
                loadModuleFromJar(jarFile, moduleMap, dependencyGraph);
            } catch (Exception e) {
                logger.error("Failed to load JAR file: {}", jarFile.getName(), e);
            }
        }

        return sortModules(moduleMap, dependencyGraph);
    }

    private static void loadModuleFromJar(File jarFile, Map<String, MBModule> moduleMap, Map<String, Set<String>> dependencyGraph) throws IOException {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, ModuleLoader.class.getClassLoader())) {
            List<MBModule> loadedModules = loadModulesFromClassLoader(classLoader);
            for (MBModule module : loadedModules) {
                String moduleName = module.getModuleInfo().name();
                moduleMap.put(moduleName, module);
                dependencyGraph.putIfAbsent(moduleName, new HashSet<>());
                ModuleConfigReader.readConfig(jarFile, moduleName, dependencyGraph);
            }
        }
    }

    private static List<MBModule> loadModulesFromClassLoader(URLClassLoader classLoader) {
        List<MBModule> modules = new ArrayList<>();
        try {
            for (Class<?> cls : getClassesFromClassLoader(classLoader)) {
                if (MBModule.class.isAssignableFrom(cls) && !cls.isInterface()) {
                    MBModule module = (MBModule) cls.getDeclaredConstructor().newInstance();
                    modules.add(module);
                    logger.info("Loaded module: {}", module.getModuleInfo().name());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load classes from class loader", e);
        }
        return modules;
    }

    private static List<Class<?>> getClassesFromClassLoader(URLClassLoader classLoader) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        for (URL url : classLoader.getURLs()) {
            File jarFile = new File(url.toURI());
            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (isValidClassEntry(entry)) {
                        String className = entry.getName().replace("/", ".").replace(".class", "");
                        try {
                            Class<?> cls = classLoader.loadClass(className);
                            classes.add(cls);
                        } catch (ClassNotFoundException e) {
                            logger.error("Class not found: {}", className, e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error reading JAR file: {}", jarFile.getName(), e);
            }
        }
        return classes;
    }

    private static boolean isValidClassEntry(JarEntry entry) {
        return entry.getName().endsWith(".class") && !entry.getName().contains("META-INF");
    }

    private static List<MBModule> sortModules(Map<String, MBModule> moduleMap, Map<String, Set<String>> dependencyGraph) {
        List<MBModule> sortedModules = new ArrayList<>();
        try {
            List<String> sortedModuleNames = ModuleSorter.topologicalSort(dependencyGraph);
            for (String moduleName : sortedModuleNames) {
                sortedModules.add(moduleMap.get(moduleName));
            }
        } catch (CircularDependencyException e) {
            logger.error("Failed to sort modules: {}", e.getMessage());
        }
        return sortedModules;
    }
}