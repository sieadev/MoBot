package net.vitacraft.api.info;

/**
 * The {@code ModuleInfo} class encapsulates information about a module, including
 * its name, version, description, author, and startup priority.
 * This class provides
 * a way to store and retrieve essential metadata about a module.
 * <p>
 * Each {@code ModuleInfo} instance is immutable, meaning that once an object is created,
 * its state cannot be modified. This ensures that the module information remains
 * consistent and reliable throughout the application's lifecycle.
 * </p>
 *
 * <pre>
 * Example usage:
 * ModuleInfo moduleInfo = new ModuleInfo("ExampleModule", "1.0", "An example module", "John Doe", StartUpPriority.HIGH);
 * </pre>
 */
public record ModuleInfo(String name, String version, String description, String author, StartUpPriority priority) {
    /**
     * Constructs a new {@code ModuleInfo} object with the specified name, version,
     * description, author, and startup priority.
     *
     * @param name        the name of the module
     * @param version     the version of the module
     * @param description a brief description of the module
     * @param author      the author of the module
     * @param priority    the startup priority of the module
     */
    public ModuleInfo {
    }
}
