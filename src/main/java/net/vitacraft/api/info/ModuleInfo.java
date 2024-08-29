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
 *
 */
public class ModuleInfo {
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final StartUpPriority priority;

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
    public ModuleInfo(final String name, final String version, final String description, final String author, final StartUpPriority priority) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.priority = priority;
    }

    /**
     * Returns the name of the module.
     *
     * @return the name of the module
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the module.
     *
     * @return the version of the module
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns a brief description of the module.
     *
     * @return the description of the module
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the author of the module.
     *
     * @return the author of the module
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the startup priority of the module.
     *
     * @return the startup priority of the module
     */
    public StartUpPriority getPriority() {
        return priority;
    }
}
