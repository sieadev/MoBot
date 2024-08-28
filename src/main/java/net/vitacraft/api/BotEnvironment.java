package net.vitacraft.api;

import net.dv8tion.jda.api.sharding.ShardManager;
import net.vitacraft.MoBot;
import net.vitacraft.manager.CommandManager;

public class BotEnvironment {
    private final ShardManager shardManager;
    private final MoBot moBot;
    private final CommandManager commandManager;

    public BotEnvironment(ShardManager shardManager, MoBot moBot, CommandManager commandManager) {
        this.shardManager = shardManager;
        this.moBot = moBot;
        this.commandManager = commandManager;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public MoBot getInstance() {
        return moBot;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
}
