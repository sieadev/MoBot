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

public class MBModule {
    private BotEnvironment botEnvironment;
    private final ModuleInfo moduleInfo;
    private final Logger logger;
    private final ConfigLoader configLoader;

    public MBModule(){
        moduleInfo = retrieveModuleInfo();
        logger = LoggerFactory.getLogger(moduleInfo.getName());
        configLoader = generateConfig();
    }

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

    private ConfigLoader generateConfig() {
        try {
            Path configDir = Paths.get(moduleInfo.getName());
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

    public void preEnable(PrimitiveBotEnvironment primitiveBotEnvironment){

    }

    public void onEnable(){

    }

    public void onDisable(){

    }

    public void postDisable(){

    }

    public void setBotEnvironment(BotEnvironment botEnvironment) {
        this.botEnvironment = botEnvironment;
    }

    public void registerSlashCommand(CommandData data, SlashCommandAddon addon){
        botEnvironment.getCommandManager().registerCommand(data, addon);
    }

    public void registerEventListener(Object... listeners){
        botEnvironment.getShardManager().addEventListener(listeners);
    }

    public BotEnvironment getBotEnvironment() {
        return botEnvironment;
    }

    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    public Logger getLogger() {
        return logger;
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
}
