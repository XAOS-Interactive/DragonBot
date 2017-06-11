package com.xaosia.dragonbot.commands.management;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.*;

public class RolesCommand extends Command {

    public RolesCommand() {
        super("roles", CommandType.TRUSTED, "");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {

        StringBuilder roleList = new StringBuilder("This guild has the following roles: \n");

        for (Role role : guild.getRoles()) {

            roleList.append("Name: " + role.getName() + "\n");
            roleList.append("ID: " + role.getId() + "\n");
            //todo: permissions
        }

        int time = 5 * 60;

        Chat.sendMessage(roleList.toString(), channel, time);

        return true;
    }

}
