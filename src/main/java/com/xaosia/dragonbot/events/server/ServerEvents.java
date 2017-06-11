package com.xaosia.dragonbot.events.server;


import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.events.chat.ChatEvents;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class ServerEvents extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {

        Dragon.getLatch().countDown();
        Bot.updateUsers();
        Dragon.getClient().getPresence().setStatus(OnlineStatus.ONLINE);


        ChatEvents.messages = new HashMap<>();
        ChatEvents.amount = new HashMap<>();

        for (Guild guild : Dragon.getClient().getGuilds()) { //TODO Replace with roles command
            for (Role role : guild.getRoles()) {
                Dragon.getLog().info(guild.getName() + " - \"" + role.getName() + "\" ID: \"" + role.getId() + "\" - " + role.getPermissions());
            }
        }

        Dragon.getLog().info("Trackers Set");
        if (Dragon.getConfig().getLogChannel() != null) {
            Chat.sendMessage("Trackers Set", Dragon.getClient().getTextChannelById(Dragon.getConfig().getLogChannel()));
        }

        Dragon.setEnabled(true);

        Dragon.getLog().info("Bot ready.");
        if (Dragon.getConfig().getLogChannel() != null) {
            Chat.sendMessage("Bot ready!", Dragon.getClient().getTextChannelById(Dragon.getConfig().getLogChannel()));
        }

    }


    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        //register the guild
        GuildManager.registerGuild(event.getGuild());
    }

}
