package net.vitacraft;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.vitacraft.api.BotEnvironment;
import net.vitacraft.api.MBModule;
import net.vitacraft.api.PrimitiveBotEnvironment;
import net.vitacraft.api.config.ConfigUtil;
import net.vitacraft.exceptions.BotStartupException;
import net.vitacraft.manager.CommandManager;
import org.simpleyaml.configuration.ConfigurationSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MoBot {
    private final List<MBModule> modules = new ArrayList<>();
    private final BotEnvironment botEnvironment;
    private final Logger logger;

    public MoBot() {
        //Initialize the Logger
        logger = LoggerFactory.getLogger("MoBot");
        logger.info("Initializing MoBot...");

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
        modules.sort(Comparator.comparing(module -> module.getModuleInfo().getPriority()));

        //Call the preEnable method on all Modules
        for (MBModule module : modules) {
            try {
                module.preEnable(primitiveBotEnvironment);
                logger.info("Pre-enabled module: {}", module.getModuleInfo().getName());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }

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
                logger.info("Successfully Enabled module {}", module.getModuleInfo().getName());
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

    private List<MBModule> loadModulesFromClassLoader(URLClassLoader classLoader) {
        List<MBModule> modules = new ArrayList<>();
        File modulesDir = new File("modules");
        File[] jarFiles = modulesDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles != null) {
            for (File jarFile : jarFiles) {
                try (URLClassLoader tempClassLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, classLoader)) {
                    for (Class<?> cls : getClassesFromJar(jarFile, tempClassLoader)) {
                        if (MBModule.class.isAssignableFrom(cls) && !cls.isInterface()) {
                            MBModule module = (MBModule) cls.getDeclaredConstructor().newInstance();
                            modules.add(module);
                            logger.info("Loaded module: {}", cls.getName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to load classes from JAR file: {}", jarFile.getName(), e);
                }
            }
        }
        return modules;
    }

    private List<Class<?>> getClassesFromJar(File jarFile, URLClassLoader classLoader) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
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
        return classes;
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