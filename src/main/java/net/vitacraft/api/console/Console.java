package net.vitacraft.api.console;

import net.vitacraft.MoBot;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Console {
    private final Map<String, ConsoleCommand> commands = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private final MoBot moBot;
    private final Logger logger;

    public Console(MoBot moBot) {
        new Thread(this::listenForCommands).start();
        this.moBot = moBot;
        this.logger = moBot.getLogger();
        registerDefaults();
    }

    private void listenForCommands() {
        while (true) {
            String input = scanner.nextLine();
            String[] parts = input.split(" ");
            String commandName = parts[0];
            String[] args = new String[parts.length - 1];
            System.arraycopy(parts, 1, args, 0, args.length);

            ConsoleCommand command = commands.get(commandName);
            if (command != null) {
                command.execute(args);
            } else {
                logger.warn("Unknown command: {}", commandName);
            }
        }
    }

    public void registerCommand(String name, ConsoleCommand command) {
        commands.put(name, command);
        logger.info("Registered command: {}", name);
    }

    public void dispatchCommand(String name, String[] args) {
        ConsoleCommand command = commands.get(name);
        if (command != null) {
            command.execute(args);
        } else {
            logger.warn("Unknown command: {}", name);
        }
    }

    private void registerDefaults() {
        registerCommand("help", args -> {
            logger.info("Available commands:");
            for (String command : commands.keySet()) {
                logger.info(" - {}", command);
            }
        });
        registerCommand("clear", args -> {
            ConsoleUtil.clearConsole();
        });
        registerCommand("shutdown", args -> {
            System.exit(0);
        });
        registerCommand("stop", args -> { System.exit(0); });
    }
}