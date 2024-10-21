package net.vitacraft.api;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.vitacraft.api.addons.SlashCommandAddon;
import net.vitacraft.api.config.ConfigLoader;
import net.vitacraft.api.info.ModuleInfo;
import net.vitacraft.api.info.StartUpPriority;
import org.simpleyaml.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents a module within the bot,
 * encapsulating its configuration, lifecycle methods, and interactions with the bot environment.
 * <p>
 * This class provides methods for module initialization, configuration, and interaction with the bot environment, including
 * registering commands and event listeners.
 * </p>
 */
public class MBModule {
    private BotEnvironment botEnvironment;
    private final ModuleInfo moduleInfo;
    private final Logger logger;
    private final ConfigLoader configLoader;

    /**
     * Constructs a new {@link MBModule} instance.
     * <p>
     * Initializes the module information, logger, and configuration loader.
     * </p>
     */
    public MBModule(){
        moduleInfo = retrieveModuleInfo();
        logger = LoggerFactory.getLogger(moduleInfo.name());
        configLoader = generateConfig();
    }

    /**
     * Retrieves module information from the module's configuration file.
     *
     * @return a {@link ModuleInfo} instance containing the module's metadata
     */
    private ModuleInfo retrieveModuleInfo() {
        ConfigLoader configLoader = new ConfigLoader(this.getClass(), "module.yml");
        ConfigurationSection config = configLoader.getConfig();
        String name = config.getString("name");
        String version = config.getString("version");
        String description = config.getString("description");
        String author = config.getString("author");
        StartUpPriority startUpPriority;

        try {
            startUpPriority = StartUpPriority.valueOf(config.getString("priority"));
        } catch (IllegalArgumentException e) {
            startUpPriority = StartUpPriority.DEFAULT;
        }

        return new ModuleInfo(name, version, description, author, startUpPriority);
    }

    /**
     * Generates and saves the module's configuration file.
     *
     * @return a {@link ConfigLoader} instance for the module's configuration
     */
    private ConfigLoader generateConfig() {
        try {
            Path configDir = Paths.get("module" + moduleInfo.name());
            Files.createDirectories(configDir); // Ensure the directory exists
            String configFilePath = configDir.resolve("config.yml").toString();
            ConfigLoader configLoader = new ConfigLoader(configFilePath);
            configLoader.save();
            return configLoader;
        } catch (Exception e) {
            logger.error("Failed to generate configuration", e);
        }
        return null;
    }

    /**
     * Called before the bot is enabled.
     * <p>
     * This method is intended to be overridden by subclasses to perform any necessary initialization
     * before the module is enabled.
     * </p>
     *
     * @param primitiveBotEnvironment the bot environment provided for the module's pre-enabling setup
     */
    public void preEnable(PrimitiveBotEnvironment primitiveBotEnvironment){
        // To be overridden by subclasses
    }

    /**
     * Called after the bot is disabled.
     * <p>
     * This method is intended to be overridden by subclasses to perform any necessary actions
     * when the module is disabled.
     * </p>
     */
    public void onEnable(){
        // To be overridden by subclasses
    }

    /**
     * Called before the bot is disabled.
     * <p>
     * This method is intended to be overridden by subclasses to perform any necessary cleanup
     * after the module has been disabled.
     * </p>
     */
    public void onDisable(){
        // To be overridden by subclasses
    }

    /**
     * Called after the bot is disabled.
     * <p>
     * This method is intended to be overridden by subclasses to perform any necessary cleanup
     * after the module has been disabled.
     * </p>
     */
    public void postDisable(){
        // To be overridden by subclasses
    }

    /**
     * Sets the {@link BotEnvironment} for this module.
     *
     * @param botEnvironment the {@link BotEnvironment} to be set
     */
    public void setBotEnvironment(BotEnvironment botEnvironment) {
        this.botEnvironment = botEnvironment;
    }

    /**
     * Registers a slash command with the bot's command manager.
     *
     * @param data the {@link CommandData} for the slash command
     * @param addon the {@link SlashCommandAddon} to handle the slash command
     */
    public void registerSlashCommand(CommandData data, SlashCommandAddon addon){
        botEnvironment.getCommandManager().registerCommand(data, addon);
    }

    /**
     * Registers event listeners with the bot's shard manager.
     *
     * @param listeners the event listeners to be registered
     */
    public void registerEventListener(Object... listeners){
        botEnvironment.getShardManager().addEventListener(listeners);
    }

    /**
     * Returns the {@link BotEnvironment} for this module.
     *
     * @return the {@link BotEnvironment} instance
     */
    public BotEnvironment getBotEnvironment() {
        return botEnvironment;
    }

    /**
     * Returns the {@link ModuleInfo} for this module.
     *
     * @return the {@link ModuleInfo} instance
     */
    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    /**
     * Returns the {@link Logger} for this module.
     *
     * @return the {@link Logger} instance
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the {@link ConfigLoader} for this module.
     *
     * @return the {@link ConfigLoader} instance
     */
    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}
