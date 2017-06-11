package com.xaosia.dragonbot.guilds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GuildConfig {

    private File configFile;
    private static Object file;
    private static JSONArray fileObj;

    private String id, prefix, logChannel, commandId, musicChannelId, mutedRoleId;
    private List<String> mutedUsers, trustedRoles, musicCommandChannels;
    private HashMap<String, String> memes;
    private boolean setup, filterSpam;

    public GuildConfig(String id) {
        this.id = id;
        this.prefix = "!";
        this.logChannel = "";
        this.commandId = "";
        this.musicChannelId = "";
        this.mutedRoleId = "";

        this.musicCommandChannels = new ArrayList<>();
        this.mutedUsers = new ArrayList<>();
        this.trustedRoles = new ArrayList<>();
        this.memes = new HashMap<>();
        this.filterSpam = false;
        this.setup = false;
    }

    public static Object getFile() {
        return file;
    }

    public File getConfigFile() {
        return configFile;
    }

    public void load() throws IOException {

        configFile = new File(Dragon.getGuildsDir(), id + ".json");

        if (!configFile.exists()) {
            configFile.createNewFile();
            save();
            Dragon.getLog().info("Created new guild config for guild: " + id);
        }

        RandomAccessFile fin;
        byte[] buffer;

        fin = new RandomAccessFile(configFile, "r");
        buffer = new byte[(int) fin.length()];
        fin.readFully(buffer);
        fin.close();

        String json = new String(buffer);
        GuildConfig file = new Gson().fromJson(json, GuildConfig.class);

        this.prefix = file.prefix;
        this.logChannel = file.logChannel;
        this.commandId = file.commandId;
        this.musicChannelId = file.musicChannelId;
        this.mutedRoleId = file.mutedRoleId;

        this.musicCommandChannels = file.musicCommandChannels;
        this.mutedUsers = file.mutedUsers;
        this.trustedRoles = file.trustedRoles;
        this.memes = file.memes;
        this.filterSpam = file.filterSpam;
        this.setup = file.setup;

        //Core.log.info(file.toString());
    }

    public void save() throws IOException {
        BufferedWriter fout;
        fout = new BufferedWriter(new FileWriter(configFile));
        fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        fout.close();
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getLogChannel() {
        return logChannel;
    }

    public void setLogChannel(String logChannel) {
        this.logChannel = logChannel;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getMusicChannelId() {
        return musicChannelId;
    }

    public List<String> getMusicCommandChannels() {
        return musicCommandChannels;
    }

    public void setMusicChannelId(String musicChannelId) {
        this.musicChannelId = musicChannelId;
    }

    public String getMutedRoleId() {
        return mutedRoleId;
    }

    public void setMutedRoleId(String mutedRoleId) {
        this.mutedRoleId = mutedRoleId;
    }

    public List<String> getMutedUsers() {
        return mutedUsers;
    }

    public List<String> getTrustedRoles() {
        return trustedRoles;
    }

    public HashMap<String, String> getMemes() {
        return memes;
    }

    public boolean isFilterSpamEnabled() {
        return filterSpam;
    }

    public void setFilterSpam(boolean filterSpam) {
        this.filterSpam = filterSpam;
    }

    public boolean isTrusted(Member member) {
        for (String id : getTrustedRoles()) {
            if (Dragon.getDiscord().userHasRoleId(member, id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSetup() {
        return setup;
    }

    public void setSetup(boolean setup) {
        this.setup = setup;
    }

}
