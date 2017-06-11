package com.xaosia.dragonbot.commands.master;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.exceptions.CommandException;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;


public class ReloadCommand extends Command {

    public ReloadCommand() {
        super("reload", CommandType.MASTER, null, "rl");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message);

        try {
            Dragon.getConfig().load();
            Bot.logGuildMessage("Config reloaded!");
            Dragon.getLog().info("Config reloaded!");
        } catch (IOException e) {
            Dragon.getLog().error("Error reloading config", e);
            throw new CommandException("Error reloading config");
        }

        return true;
    }

}
