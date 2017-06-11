package com.xaosia.dragonbot.commands.management;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.ChannelLogger;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class SetCmdCommand extends Command {

    public SetCmdCommand() {
        super("setcmd", CommandType.TRUSTED, "<Channel>");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {

        if (args.length == 0) {
            return false;
        }

        String channelId = args[0];
        if (guild.getTextChannelById(channelId) == null) {
            Chat.sendMessage(sender.getAsMention() + " You must provide a valid channel id.", channel, 20);
            return true;
        }

        //chanel id is correct, save it and check if log channel is set, if so then set setup to true
        GuildManager.getGuildConfig(guild).setCommandId(channelId);

        try {
            GuildManager.getGuildConfig(guild).save();
            Chat.sendMessage(sender.getAsMention() + " The command channel has been set to " + guild.getTextChannelById(channelId).getAsMention() + ".", channel, 20);

            if (GuildManager.getGuildConfig(guild).getLogChannel() != null) {
                GuildManager.getGuildConfig(guild).setSetup(true);
                Chat.sendMessage(sender.getAsMention() + " Setup is now complete.", channel, 20);
            } else  {
                return true;
            }

        } catch (Exception ex) {
            Dragon.getLog().error("Failed to update config for guild: " + guild.getId());
        }


        return true;
    }

}
