package com.xaosia.dragonbot.commands;

import com.xaosia.dragonbot.exceptions.CommandException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public abstract class Command {

    private String name;
    private String[] aliases;
    private String usage;
    private CommandType type;
    private boolean enabled = true;

    public enum CommandType {
        GENERAL,
        TRUSTED,
        MUSIC,
        MASTER
    }

    protected Command(String name, CommandType type, String usage, String... aliases) {
        this.name = name;
        this.aliases = aliases;
        this.usage = usage;
        this.type = type;
    }

    public abstract boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException;

    /**
     * Get the name of the command used after the prefix
     *
     * @return The command name.
     */
    public String getName() {
        return name;
    }

    public String[] getAliases() {
        return aliases;
    }

    public String getUsage() {
        return getName() + " " + usage;
    }

    public CommandType getType() {
        return type;
    }

    public void setEnabled(boolean enable) {
        this.enabled = enable;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
