package com.xaosia.dragonbot.guilds;

import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.entities.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static void unregisterGuiild(Guild guild) {
        try {

            //make sure teh guild is registered
            if (!guilds.containsKey(guild.getId())) {
                return;
            }

            //delete the guild config and remove it from the list of guilds
            if (guilds.get(guild.getId()).getConfigFile().delete()) {
                Dragon.getLog().info("Successfully unregistered guild: " + guild.getId());
            }

        } catch (Exception ex) {
            Dragon.getLog().error("Failed to unregister guild: " + guild.getId(), ex);
        }
    }

    public static GuildConfig getGuildConfig(Guild guild) {
        if (guilds.containsKey(guild.getId())) {
            return guilds.get(guild.getId());
        }
        return new GuildConfig(guild.getId());
    }

    public static List<String> getMusicCommandChannels() {
        List<String> musicCommandChannels = new ArrayList<>();
        for (GuildConfig guildConfig : guilds.values()) {
            musicCommandChannels.add(guildConfig.getCommandId());
        }
        return musicCommandChannels;
    }

}
