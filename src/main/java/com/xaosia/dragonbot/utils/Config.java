package com.xaosia.dragonbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.entities.Member;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private int maxMessageLength;
    private String masterID, mainGuildID, punishmentLogID, logChannel, mainMusicChannelID, mutedRoleID, discordToken, googleAPIKey, secretKey, commandPrefix;
    private List<String> blockedUsers, trustedRoles, musicVoiceChannels, musicCommandChannels;
    private boolean filterSpam;

    private static File settingsFile = new File("settings.json");

    public Config()  {
        maxMessageLength = 275;
        masterID = "";
        mainGuildID = "";
        punishmentLogID = "";
        logChannel = "";
        mainMusicChannelID = "";
        mutedRoleID = "";

        discordToken = "";
        googleAPIKey = "";
        secretKey = "";
        commandPrefix = "!";

        blockedUsers = new ArrayList<>();
        trustedRoles = new ArrayList<>();
        musicVoiceChannels = new ArrayList<>();
        musicCommandChannels = new ArrayList<>();

        filterSpam = false;
    }

    public void save() throws IOException {
        BufferedWriter fout;
        fout = new BufferedWriter(new FileWriter(settingsFile));
        fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        fout.close();
    }

    public void load() throws IOException {

        RandomAccessFile fin;
        byte[] buffer;

        fin = new RandomAccessFile(settingsFile, "r");
        buffer = new byte[(int) fin.length()];
        fin.readFully(buffer);
        fin.close();

        String json = new String(buffer);
        Config file = new Gson().fromJson(json, Config.class);
        maxMessageLength = file.maxMessageLength;
        masterID = file.masterID;
        mainGuildID = file.mainGuildID;
        punishmentLogID = file.punishmentLogID;
        logChannel = file.logChannel;
        mainMusicChannelID = file.mainMusicChannelID;
        mutedRoleID = file.mutedRoleID;

        discordToken = file.discordToken;
        googleAPIKey = file.googleAPIKey;
        secretKey = file.secretKey;
        commandPrefix = file.commandPrefix;

        blockedUsers = file.blockedUsers;
        trustedRoles = file.trustedRoles;
        musicCommandChannels = file.musicCommandChannels;
        musicVoiceChannels = file.musicVoiceChannels;

        filterSpam = file.filterSpam;
    }

    public int getMaxMessageLength() {
        return maxMessageLength;
    }

    public String getMasterID() {
        return masterID;
    }

    public String getMainGuildID() {
        return mainGuildID;
    }

    public String getPunishmentLogID() {
        return punishmentLogID;
    }

    public String getLogChannel() {
        return logChannel;
    }

    public String getMainMusicChannelID() {
        return mainMusicChannelID;
    }

    public String getMutedRoleID() {
        return mutedRoleID;
    }

    public String getDiscordToken() {
        return discordToken;
    }

    public String getGoogleAPIKey() {
        return googleAPIKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public List<String> getBlockedUsers() {
        return blockedUsers;
    }

    public List<String> getTrustedRoles() {
        return trustedRoles;
    }

    public List<String> getMusicVoiceChannels() {
        return musicVoiceChannels;
    }

    public List<String> getMusicCommandChannels() {
        return musicCommandChannels;
    }

    public boolean isFilterSpamEnabled() {
        return filterSpam;
    }

    public File getSettingsFile() {
        return settingsFile;
    }

    public boolean isTrusted(Member member) {
        for (String id : getTrustedRoles()) {
            if (Dragon.getDiscord().userHasRoleId(member, id)) {
                return true;
            }
        }
        return false;
    }
}
