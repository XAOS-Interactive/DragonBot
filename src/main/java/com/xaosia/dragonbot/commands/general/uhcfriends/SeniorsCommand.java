package com.xaosia.dragonbot.commands.general.uhcfriends;


import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.commands.Command;
import net.dv8tion.jda.core.entities.*;

public class SeniorsCommand extends Command{

    public SeniorsCommand() {
        super("seniors", Command.CommandType.GENERAL, null);
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {

        Role seniors = Dragon.getClient().getGuildById("292061198373879809").getRoleById("298857708407357451");
        StringBuilder tagMsg = new StringBuilder(sender.getUser().getName() + " is requesting help. Tagging eligible online staff to help. ");

        for (Member member : guild.getMembers()) {
            if (Dragon.getDiscord().userHasRoleId(member, seniors.getId())) {
                tagMsg.append(member.getAsMention()).append(" ");
            }
        }

        Chat.sendMessage(tagMsg.toString(), channel);

        return true;
    }

}
