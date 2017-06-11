package com.xaosia.dragonbot.commands.general;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.io.IOException;
import java.util.Arrays;
import com.google.common.base.Joiner;


public class MemeCommand extends Command {

    public MemeCommand() {
        super("meme", CommandType.GENERAL, "<name|add|remove> <name> <meme>");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {

        if (args.length == 0) {
            Chat.sendMessage(":eye: :tongue: :eye:", channel);
            return true;
        }

        if (GuildManager.getGuildConfig(guild).getMemes().containsKey(args[0])) {
            Chat.sendMessage(GuildManager.getGuildConfig(guild).getMemes().get(args[0]), channel);
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {

            if (args.length < 3) {
                return false;
            }

            String name = args[1];
            String memeMsg = Joiner.on(' ').join(Arrays.copyOfRange(args, 2, args.length));

            GuildManager.getGuildConfig(guild).getMemes().put(name, memeMsg);

            try {
                GuildManager.getGuildConfig(guild).save();
            } catch (IOException ex) {
                Chat.sendMessage(sender.getAsMention() + " failed to save meme, an unknown error occurred.", channel);
                throw new CommandException("Failed to save memes config");
            }

            Chat.sendMessage(sender.getAsMention() + " added " + name + " meme sucessfully!", channel, 30);

            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {

            if (args.length < 2) {
                return false;
            }

            String name = args[1];

            if (GuildManager.getGuildConfig(guild).getMemes().containsKey(name)) {

                GuildManager.getGuildConfig(guild).getMemes().remove(name);

                try {
                    GuildManager.getGuildConfig(guild).save();
                } catch (IOException ex) {
                    Chat.sendMessage(sender.getAsMention() + " failed to remove meme, an unknown error occurred.", channel);
                    throw new CommandException("Failed to save memes config");
                }

                Chat.sendMessage(sender.getAsMention() + " removed " + name + " meme sucessfully!", channel, 30);
                return true;

            } else {

                Chat.sendMessage(sender.getAsMention() + " Could not find the " + name + " meme", channel, 30);
                return true;

            }

        }

        return true;
    }

}
