package net.vitacraft.api.addons;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a command that can be executed in response to a slash command interaction in Discord.
 */
public interface SlashCommandAddon {

    /**
     * Executes the command when a slash command interaction is received.
     * This method is called when the command is executed by a user.
     *
     * @param event the SlashCommandInteractionEvent containing information about the command interaction
     */
    void execute(@NotNull SlashCommandInteractionEvent event);
}