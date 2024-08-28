package net.vitacraft;

import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.vitacraft.api.BotEnvironment;
import net.vitacraft.api.MBModule;
import net.vitacraft.api.PrimitiveBotEnvironment;
import net.vitacraft.api.config.ConfigUtil;
import net.vitacraft.exceptions.BotStartupException;
import net.vitacraft.manager.CommandManager;
import org.simpleyaml.configuration.ConfigurationSection;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MoBot {
    private final List<MBModule> modules = new ArrayList<>();
    private final BotEnvironment botEnvironment;
    private final Logger logger;

    public MoBot() {
        //Initialize the Logger
        logger = LoggerFactory.getLogger(MoBot.class);
        logger.info("Initializing MoBot...");

        //Generate the DefaultShardManagerBuilder without initializing it
        DefaultShardManagerBuilder builder = getBuilder();

        //Set up the PrimitiveBotEnvironment and pass in all data available pre enabling
        PrimitiveBotEnvironment primitiveBotEnvironment = new PrimitiveBotEnvironment(builder, this);

        //Load all modules
        modules.addAll(loadModules());

        //Call the preEnable method on all Modules
        for (MBModule module : modules) {
            try {
                module.preEnable(primitiveBotEnvironment);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        //Start the bot and construct the ShardManager
        ShardManager shardManager;
        try{
            shardManager = enableBot(builder);
        } catch (BotStartupException e) {
            botEnvironment = null;
            logger.error("Bot startup failed", e);
            return;
        }

        //Initialize the CommandManager
        CommandManager commandManager = new CommandManager();

        //Set up the BotEnvironment
        botEnvironment = new BotEnvironment(shardManager, this, commandManager);

        //Register the CommandManager
        shardManager.addEventListener(commandManager);

        //Call the onEnable method on all Modules
        for (MBModule module : modules) {
            module.setBotEnvironment(botEnvironment);
            try {
                module.onEnable();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private DefaultShardManagerBuilder getBuilder(){
        ConfigUtil configUtil = new ConfigUtil("./bot.yml");
        configUtil.save();
        ConfigurationSection config = configUtil.getConfig();
        String token = config.getString("token");

        return DefaultShardManagerBuilder.createDefault(token);
    }

    private ShardManager enableBot(DefaultShardManagerBuilder builder) throws BotStartupException {
        ShardManager shardManager;

        try {
            shardManager = builder.build();
        } catch (IllegalArgumentException e) {
            throw new BotStartupException("Invalid token. Disabling...");
        } catch (Exception e) {
            throw new BotStartupException("An unknown error occurred while setting up the shard manager.");
        }

        return shardManager;
    }

    private List<MBModule> loadModules() {
        List<MBModule> modules = new ArrayList<>();

        Reflections reflections = new Reflections("net.vitacraft");
        Set<Class<? extends MBModule>> moduleClasses = reflections.getSubTypesOf(MBModule.class);

        for (Class<? extends MBModule> moduleClass : moduleClasses) {
            try {
                modules.add(moduleClass.getDeclaredConstructor().newInstance());
                logger.info("Loaded module: {}", moduleClass.getName());
            } catch (Exception e) {
                logger.error("Failed to load module: {}", moduleClass.getName());
                logger.error(e.getMessage());
            }
        }

        return modules;
    }

    public void shutdown() {
        for (MBModule module : modules) {
            module.onDisable();
        }

        if (botEnvironment.getShardManager() != null) {
            botEnvironment.getShardManager().shutdown();
        }

        for (MBModule module : modules) {
            module.postDisable();
        }
    }

    public static void main(String[] args) {
        MoBot bot = new MoBot();
        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
    }
}