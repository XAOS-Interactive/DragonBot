package com.xaosia.dragonbot.commands.management;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class AddTrustedCommand extends Command {

    public AddTrustedCommand() {
        super("addtrusted", CommandType.TRUSTED, "<role>");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {

        if (args.length == 0) {
            return false;
        }

        String role = args[0];
        if (guild.getRoleById(role) == null) {
            Chat.sendMessage(sender.getAsMention() + " You must provide a valid role id, to get a list of roles, use !roles.", channel, 20);
            return true;
        }

        //chanel id is correct, save it and check if log channel is set, if so then set setup to true
        GuildManager.getGuildConfig(guild).getTrustedRoles().add(role);

        try {
            GuildManager.getGuildConfig(guild).save();
            Chat.sendMessage(sender.getAsMention() + " The role " + guild.getRoleById(role).getName() + " has been added as a trusted role.", channel, 20);
        } catch (Exception ex) {
            Dragon.getLog().error("Failed to update config for guild: " + guild.getId());
        }

        return true;
    }

}
