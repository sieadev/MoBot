package net.vitacraft;

import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.vitacraft.api.BotEnvironment;
import net.vitacraft.api.MBModule;
import net.vitacraft.api.PrimitiveBotEnvironment;
import net.vitacraft.api.classloader.ModuleLoader;
import net.vitacraft.api.config.ConfigLoader;
import net.vitacraft.api.console.Console;
import net.vitacraft.exceptions.BotStartupException;
import net.vitacraft.manager.CommandManager;
import net.vitacraft.api.console.ConsoleUtil;
import org.simpleyaml.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.*;

/**
 * The main class for initializing and managing the MoBot application.
 * <p>
 * This class handles the initialization of the bot, loading of modules,
 * setting up the bot environment, and managing the lifecycle of the bot.
 * </p>
 */
public class MoBot {
    private final List<MBModule> modules = new ArrayList<>();
    private final BotEnvironment botEnvironment;
    private final Logger logger;
    private Console console;

    public MoBot() {
        ConsoleUtil.clearConsole();

        // Initialize the Logger
        logger = LoggerFactory.getLogger("MoBot");

        // Generate the DefaultShardManagerBuilder without initializing it
        DefaultShardManagerBuilder builder = getBuilder();

        // Set up the PrimitiveBotEnvironment and pass in all data available pre enabling
        PrimitiveBotEnvironment primitiveBotEnvironment = new PrimitiveBotEnvironment(builder, this);

        // Create the modules directory if it does not exist
        createModulesDirectory();

        // Load all modules
        modules.addAll(ModuleLoader.loadModules(System.getProperty("user.dir") + "/modules"));
        logger.info("Loaded MoBot modules: {}", modules.size());

        // Sort modules based on priority
        modules.sort(Comparator.comparing(module -> module.getModuleInfo().priority()));

        // Call the preEnable method on all Modules
        List<String> enabledModules = new ArrayList<>();
        for (MBModule module : modules) {
            try {
                module.preEnable(primitiveBotEnvironment);
                enabledModules.add(module.getModuleInfo().name());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        logger.info("Pre-enabled modules: {}", enabledModules);

        // Start the bot and construct the ShardManager
        ShardManager shardManager;
        try {
            shardManager = enableBot(builder);
            logger.info("Successfully enabled shard manager with {} shards.", shardManager.getShardsTotal());
        } catch (BotStartupException e) {
            botEnvironment = null;
            logger.error("Bot startup failed: " + e.getMessage());
            return;
        }

        // Initialize the CommandManager
        CommandManager commandManager = new CommandManager();

        // Set up the BotEnvironment
        botEnvironment = new BotEnvironment(shardManager, this, commandManager);

        // Register the CommandManager
        shardManager.addEventListener(commandManager);

        // Call the onEnable method on all Modules
        for (MBModule module : modules) {
            module.setBotEnvironment(botEnvironment);
            try {
                module.onEnable();
                logger.info("Successfully Enabled module {}", module.getModuleInfo().name() + " by " + module.getModuleInfo().authors());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

        // Initialize the Console
        console = new Console(this);
    }

    private DefaultShardManagerBuilder getBuilder() {
        ConfigLoader configLoader = new ConfigLoader("./bot.yml");
        configLoader.save();
        ConfigurationSection config = configLoader.getConfig();
        String token = config.getString("token");
        List<String> gateWayIntents = config.getStringList("gateway-intents");

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(token);

        for (String intent : gateWayIntents) {
            builder.enableIntents(GatewayIntent.valueOf(intent));
        }

        return builder;
    }

    private ShardManager enableBot(DefaultShardManagerBuilder builder) throws BotStartupException {
        ShardManager shardManager = null;
        Scanner scanner = new Scanner(System.in);

        ConfigLoader configLoader = new ConfigLoader("./bot.yml");
        ConfigurationSection config = configLoader.getConfig();
        String token = config.getString("token");

        if (token == null || token.isEmpty()) {
            ConsoleUtil.print("No Discord Bot-Token found. This might be your first time running the bot. Please enter a valid bot token: ");
            token = scanner.nextLine();
            config.set("token", token);
            configLoader.save();
            builder.setToken(token);
        }

        while (shardManager == null) {
            try {
                shardManager = builder.build();
            } catch (InvalidTokenException e) {
                logger.info("The provided Discord Bot-Token is invalid. Please enter a new token: ");
                String newToken = scanner.nextLine();
                config.set("token", newToken);
                configLoader.save();
                builder.setToken(newToken);
            } catch (Exception e) {
                throw new BotStartupException("An unknown error occurred while setting up the shard manager.", e);
            }
        }

        return shardManager;
    }

    private void createModulesDirectory() {
        File modulesDir = new File("modules");
        if (!modulesDir.exists()) {
            boolean created = modulesDir.mkdirs();
            if (created) {
                logger.info("Created modules directory.");
            } else {
                logger.error("Failed to create modules directory.");
            }
        }
    }

    public void shutdown() {
        logger.info("Shutting down MoBot...");

        for (MBModule module : modules) {
            module.onDisable();
        }

        if (botEnvironment != null && botEnvironment.getShardManager() != null) {
            botEnvironment.getShardManager().shutdown();
            logger.info("Shard manager has been shut down.");
        }

        for (MBModule module : modules) {
            module.postDisable();
        }

        logger.info("See you soon!.");
    }

    public Logger getLogger() {
        return logger;
    }

    public Console getConsole() {
        return console;
    }

    public static void main(String[] args) {
        MoBot bot = new MoBot();
        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
    }
}