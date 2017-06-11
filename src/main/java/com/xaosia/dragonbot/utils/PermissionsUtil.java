package com.xaosia.dragonbot.utils;

import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Bot;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class PermissionsUtil {

    public static boolean isBotCreator(User user) {
        return user.equals(Bot.getCreator());
    }

    public static boolean hasAdministrator(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR);
    }

    public static boolean isTrusted(Guild guild, Member member) {
        return GuildManager.getGuildConfig(guild).isTrusted(member) || isBotCreator(member.getUser());
    }

    public static boolean canUseMusicCommand(Guild guild, Channel channel) {
        return GuildManager.getGuildConfig(guild).getMusicCommandChannels().contains(channel.getId());
    }

}
