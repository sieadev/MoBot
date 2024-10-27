package net.vitacraft;

import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.vitacraft.api.BotEnvironment;
import net.vitacraft.api.MBModule;
import net.vitacraft.api.PrimitiveBotEnvironment;
import net.vitacraft.api.config.ConfigLoader;
import net.vitacraft.exceptions.BotStartupException;
import net.vitacraft.manager.CommandManager;
import net.vitacraft.utils.ConsoleUtil;
import org.simpleyaml.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    /**
     * Constructs and initializes the MoBot instance.
     * <p>
     * This constructor initializes the logger, sets up the bot environment,
     * loads and enables modules, and starts the bot.
     * </p>
     */
    public MoBot() {
        generateTitleAscii();

        //Initialize the Logger
        logger = LoggerFactory.getLogger("MoBot");

        //Generate the DefaultShardManagerBuilder without initializing it
        DefaultShardManagerBuilder builder = getBuilder();

        //Set up the PrimitiveBotEnvironment and pass in all data available pre enabling
        PrimitiveBotEnvironment primitiveBotEnvironment = new PrimitiveBotEnvironment(builder, this);

        //Create the modules directory if it does not exist
        createModulesDirectory();

        //Load all modules
        modules.addAll(loadModulesFromDirectory());
        logger.info("Loaded MoBot modules: {}", modules.size());

        //Sort modules based on priority
        modules.sort(Comparator.comparing(module -> module.getModuleInfo().priority()));

        //Call the preEnable method on all Modules
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

        //Start the bot and construct the ShardManager
        ShardManager shardManager;
        try{
            shardManager = enableBot(builder);
            logger.info("Successfully enabled shard manager");
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
                logger.info("Successfully Enabled module {}", module.getModuleInfo().name());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * Creates a {@link DefaultShardManagerBuilder} using the bot token from the configuration file.
     *
     * @return a {@link DefaultShardManagerBuilder} configured with the bot token
     */
    private DefaultShardManagerBuilder getBuilder(){
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

    /**
     * Initializes and starts the bot, creating a {@link ShardManager}.
     *
     * @param builder the {@link DefaultShardManagerBuilder} used to create the {@link ShardManager}
     * @return the created {@link ShardManager}
     * @throws BotStartupException if the bot could not be started
     */
    private ShardManager enableBot(DefaultShardManagerBuilder builder) throws BotStartupException {
        ShardManager shardManager;

        try {
            shardManager = builder.build();
        } catch (IllegalArgumentException e) {
            throw new BotStartupException("The provided Discord Bot-Token is invalid. Check the bot.yml file.");
        } catch (Exception e) {
            throw new BotStartupException("An unknown error occurred while setting up the shard manager.", e);
        }

        return shardManager;
    }

    /**
     * Loads all modules from the "modules" directory.
     *
     * @return a list of loaded {@link MBModule} instances
     */
    private List<MBModule> loadModulesFromDirectory() {
        List<MBModule> modules = new ArrayList<>();
        File modulesDir = new File("modules");
        if (modulesDir.isDirectory()) {
            File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles != null) {
                for (File jarFile : jarFiles) {
                    try (URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, getClass().getClassLoader())) {
                        modules.addAll(loadModulesFromClassLoader(classLoader));
                    } catch (Exception e) {
                        logger.error("Failed to load JAR file: {}", jarFile.getName(), e);
                    }
                }
            } else {
                logger.warn("No JAR files found in the modules directory.");
            }
        } else {
            logger.error("Modules directory is not a directory.");
        }
        return modules;
    }

    /**
     * Loads modules from a given {@link URLClassLoader}.
     *
     * @param classLoader the {@link URLClassLoader} to load classes from
     * @return a list of loaded {@link MBModule} instances
     */
    private List<MBModule> loadModulesFromClassLoader(URLClassLoader classLoader) {
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

    /**
     * Retrieves all classes from a class loader.
     *
     * @param classLoader the {@link URLClassLoader} to load classes
     * @return a list of classes found in the class loader
     * @throws Exception if an error occurs while loading classes
     */
    private List<Class<?>> getClassesFromClassLoader(URLClassLoader classLoader) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        for (URL url : classLoader.getURLs()) {
            File jarFile = new File(url.toURI());
            try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
                java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    java.util.jar.JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").replace(".class", "");
                        try {
                            Class<?> cls = classLoader.loadClass(className);
                            classes.add(cls);
                        } catch (ClassNotFoundException e) {
                            logger.error("Class not found: {}", className, e);
                        }
                    }
                }
            }
        }
        return classes;
    }

    /**
     * Creates the "modules" directory if it does not already exist.
     */
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

    /**
     * Shuts down the bot and performs cleanup tasks.
     * <p>
     * This method calls the {@link MBModule#onDisable()} method on all modules,
     * shuts down the {@link ShardManager}, and then calls {@link MBModule#postDisable()}.
     * </p>
     */
    public void shutdown() {
        for (MBModule module : modules) {
            module.onDisable();
        }

        if (botEnvironment != null && botEnvironment.getShardManager() != null) {
            botEnvironment.getShardManager().shutdown();
        }

        for (MBModule module : modules) {
            module.postDisable();
        }
    }

    /**
     * The main method to start the MoBot application.
     * <p>
     * Initializes the MoBot instance and sets up a shutdown hook to perform cleanup tasks.
     * </p>
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        MoBot bot = new MoBot();
        Runtime.getRuntime().addShutdownHook(new Thread(bot::shutdown));
    }

    public void generateTitleAscii(){
        ConsoleUtil.clearConsole();
        ConsoleUtil.print("#77DD77  __  __       ____        _   ");
        ConsoleUtil.print("#77DD77 |  \\/  | ___ | __ )  ___ | |_    #FFFFFF-  Vitacraft Development 2024");
        ConsoleUtil.print("#77DD77 | |\\/| |/ _ \\|  _ \\ / _ \\| __|   #FFFFFF-  Version: " + getClass().getPackage().getImplementationVersion());
        ConsoleUtil.print("#77DD77 | |  | | (_) | |_) | (_) | |_    #FFFFFF-  Host: " + System.getProperty("os.name"));
        ConsoleUtil.print("#77DD77 |_|  |_|\\___/|____/ \\___/ \\__|   #FFFFFF-  Memory: " + Runtime.getRuntime().maxMemory() / (1024 * 1024));
        ConsoleUtil.print("  ");
        ConsoleUtil.print("Welcome to the #77DD77MoBot CLI#FFFFFF. Type 'help' to see available commands.");
        ConsoleUtil.print("  ");
    }
}