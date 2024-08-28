package net.vitacraft.api;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.vitacraft.api.addons.SlashCommandAddon;
import net.vitacraft.api.config.ConfigUtil;
import net.vitacraft.api.info.ModuleInfo;
import net.vitacraft.api.info.StartUpPriority;
import org.simpleyaml.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class MBModule {
    private BotEnvironment botEnvironment;
    private final ModuleInfo moduleInfo;
    private final Logger logger;
    private final ConfigurationSection config;

    public MBModule(){
        moduleInfo = retrieveModuleInfo();
        config = generateConfig();
        logger = LoggerFactory.getLogger(moduleInfo.getName());
    }

    private ModuleInfo retrieveModuleInfo() {
        try {
            ConfigUtil configUtil = new ConfigUtil("./module.yml");
            configUtil.save();

            ConfigurationSection config = configUtil.getConfig();
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to load module info", e);
        }
    }

    private ConfigurationSection generateConfig() {

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
}
