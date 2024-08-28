package net.vitacraft.api.info;

public class ModuleInfo {
    private final String name;
    private final String version;
    private final String description;
    private final String author;
    private final StartUpPriority priority;

    public ModuleInfo(final String name, final String version, final String description, final String author, final StartUpPriority priority) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.author = author;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    public StartUpPriority getPriority() {
        return priority;
    }
}
