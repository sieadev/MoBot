package net.vitacraft.api;

import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.vitacraft.MoBot;

public class PrimitiveBotEnvironment {
    private final DefaultShardManagerBuilder builder;
    private final MoBot moBot;

    public PrimitiveBotEnvironment(DefaultShardManagerBuilder builder, MoBot moBot) {
        this.builder = builder;
        this.moBot = moBot;
    }

    public DefaultShardManagerBuilder getBuilder() {
        return builder;
    }

    public MoBot getInstance() {
        return moBot;
    }
}
