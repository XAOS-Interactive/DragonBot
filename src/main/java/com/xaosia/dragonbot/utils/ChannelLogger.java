package com.xaosia.dragonbot.utils;

import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.guilds.GuildConfig;
import com.xaosia.dragonbot.guilds.GuildManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;

public class ChannelLogger {

    public static void logGuildMessage(Guild guild, String message) {
        if (GuildManager.getGuildConfig(guild).getLogChannel() != null) {
            Chat.sendMessage(message, Dragon.getClient().getTextChannelById(GuildManager.getGuildConfig(guild).getLogChannel()));
        }
    }

    public static void logGuildMessage(Guild guild, MessageBuilder message) {
        if (GuildManager.getGuildConfig(guild).getLogChannel() != null) {
            Dragon.getClient().getTextChannelById(GuildManager.getGuildConfig(guild).getLogChannel()).sendMessage(message.build()).queue();
        }
    }

}
