package com.xaosia.dragonbot.commands.general;

import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", CommandType.GENERAL, null, "commands", "howdoido", "howdoidothis");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message);

        Chat.sendMessage(new MessageBuilder().append(sender.getAsMention()).setEmbed(Chat.getEmbed().setColor(Chat.CUSTOM_DARK_GREEN)
                .addField("Music",
                        "`play <term>` Replace \"term\" with a YouTube/SoundCloud url or a search query to play music\n" +
                                "`queue` Lists the current music playlist of queued messages\n" +
                                "`skip` Casts your vote to skip the song that is currently playing\n" +
                                "`random <playlist>` Plays a random song from the specified category", false)
                .addField("General",
                        "`meme <name|add|remove> <name> <meme>` Manage memes in your guild", false).build()).build(), channel, 30);

        return true;
    }

}
