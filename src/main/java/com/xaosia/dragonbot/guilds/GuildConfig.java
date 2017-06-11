package com.xaosia.dragonbot.guilds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GuildConfig {

    private static File configFile;
    private static Object file;
    private static JSONArray fileObj;

    private String id, prefix, logId, commandId, musicId;
    private JSONArray mutedUsers;

    public GuildConfig(String id) {
        this.id = id;
        this.prefix = "!";
        this.logId = "";
        this.commandId = "";
        this.musicId = "";

        this.mutedUsers = new JSONArray();

    }

    public static Object getFile() {
        return file;
    }

    public static File getConfigFile() {
        return configFile;
    }

    public void load() throws IOException {

        configFile = new File(Dragon.getGuildsDir(), id + ".json");

        if (!configFile.exists()) {
            Dragon.getLog().info("Creating new guild config for guild: " + id);
            configFile.createNewFile();
        }

        Gson gson = new Gson();
        JsonElement json = gson.fromJson(new FileReader(configFile), JsonElement.class);

        file = gson.toJson(json);
        fileObj = new JSONArray(file.toString());
        //Core.log.info(file.toString());
    }

    private static void save(Object obj) throws IOException {
        BufferedWriter fout = new BufferedWriter(new FileWriter(configFile));
        fout.write(new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(obj.toString()).getAsJsonArray()));
        fout.close();

        file = obj;
        fileObj = new JSONArray(file.toString());
    }

}
