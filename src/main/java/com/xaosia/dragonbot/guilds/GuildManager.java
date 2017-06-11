package com.xaosia.dragonbot.guilds;

import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.entities.Guild;

import java.util.HashMap;

public class GuildManager {

    private final static HashMap<String, GuildConfig> guilds = new HashMap<>();

    public static void registerGuild(Guild guild) {
        try {

            //make sure the guild is not already registered
            if (guilds.containsKey(guild.getId())) {
                return;
            }

            //register the guild and attempt to load the guild config
            guilds.put(guild.getId(), new GuildConfig(guild.getId()));
            guilds.get(guild.getId()).load();

        } catch (Exception ex) {
            Dragon.getLog().error("Failed to register guild: " + guild.getId(), ex);
        }
    }

    public static GuildConfig getGuildConfig(Guild guild) {
        if (guilds.containsKey(guild.getId())) {
            return guilds.get(guild.getId());
        }
        return null;
    }

}
