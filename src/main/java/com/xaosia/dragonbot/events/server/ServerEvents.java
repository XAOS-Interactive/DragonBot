package com.xaosia.dragonbot.events.server;


import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.management.ToggleMusicCommand;
import com.xaosia.dragonbot.commands.music.SkipCommand;
import com.xaosia.dragonbot.events.chat.ChatEvents;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.ChannelLogger;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.*;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerEvents extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent event) {

        Dragon.getLatch().countDown();
        Bot.updateStatus();
        Dragon.getClient().getPresence().setStatus(OnlineStatus.ONLINE);

        Bot.nowPlaying = new ArrayList<>();
        SkipCommand.votes = new ArrayList<>();
        ChatEvents.messages = new HashMap<>();
        ChatEvents.amount = new HashMap<>();
        ToggleMusicCommand.canQueue = new HashMap<>();

        for (Guild guild : Dragon.getClient().getGuilds()) { //TODO Replace with roles command
            for (Role role : guild.getRoles()) {
                Dragon.getLog().info(guild.getName() + " - \"" + role.getName() + "\" ID: \"" + role.getId() + "\" - " + role.getPermissions());
            }
            //register guilds the bot is already in
            GuildManager.registerGuild(guild);

            if (!GuildManager.getGuildConfig(guild).isSetup()) {
                Chat.sendMessage("Hello, I am Dragon, an advanced discord mod bot created in java. I will not function properly until I have been " +
                                "setup. If you need help, please refer to my documentation.",
                        Dragon.getClient().getGuildById(guild.getId()).getPublicChannel(), 20);
            }

        }

        Dragon.getLog().info("Trackers Set");
        if (Dragon.getConfig().getLogChannel() != null) {
            Chat.sendMessage("Trackers Set", Dragon.getClient().getTextChannelById(Dragon.getConfig().getLogChannel()));
        }


        for (Guild guild : Dragon.getClient().getGuilds()) {
            try {
                VoiceChannel channel = Dragon.getClient().getVoiceChannelById(GuildManager.getGuildConfig(guild).getMusicChannelId());
                if (channel != null) {
                    channel.getGuild().getAudioManager().openAudioConnection(channel);
                }
                ToggleMusicCommand.canQueue.put(guild.getId(), true);
            } catch (Exception ex) {
                //do nothing
            }
        }

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Dragon.getLog().error("Error trusting cert", e);
        }

        Dragon.setEnabled(true);

        Dragon.getLog().info("Bot ready.");
        if (Dragon.getConfig().getLogChannel() != null) {
            Chat.sendMessage("Bot ready!", Dragon.getClient().getTextChannelById(Dragon.getConfig().getLogChannel()));
        }

    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        //register a guild the bot joins
        GuildManager.registerGuild(event.getGuild());
        Chat.sendMessage("Hello, I am Dragon, an advanced discord mod bot created in java. I will not function properly until I have been " +
                        "setup. If you need help, please refer to my documentation.",
                Dragon.getClient().getGuildById(event.getGuild().getId()).getPublicChannel(), 20);
        ToggleMusicCommand.canQueue.put(event.getGuild().getId(), true);
        Bot.updateStatus();
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        //unregister a guild that removes the bot
        GuildManager.unregisterGuiild(event.getGuild());
        Chat.sendPM("I have been removed from your guild: " + event.getGuild().getName() + ". Your guild's config has been removed, if you wish to " +
                        "add me back, you will have to redo the setup.", event.getGuild().getOwner().getUser());
        ToggleMusicCommand.canQueue.remove(event.getGuild().getId());
        Bot.updateStatus();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        User user = event.getMember().getUser();

        ChannelLogger.logGuildMessage(event.getGuild(), new MessageBuilder().setEmbed(Chat.getEmbed().setTitle(Chat.getFullName(user), null)
                .setThumbnail(user.getEffectiveAvatarUrl()).setDescription("*" + event.getMember().getAsMention() + " joined the server.*" +
                        "\n\n**Account Creation:** " + Bot.formatTime(LocalDateTime.from(user.getCreationTime())))
                .setFooter("System time | " + Bot.getBotTime(), null)
                .setColor(Chat.CUSTOM_GREEN).build()));
    }

    @Override
    public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
        User user = event.getMember().getUser();

        ChannelLogger.logGuildMessage(event.getGuild(), new MessageBuilder().setEmbed(Chat.getEmbed().setTitle(Chat.getFullName(user), null)
                .setThumbnail(user.getEffectiveAvatarUrl()).setDescription("*" + event.getMember().getAsMention() + " left the server.*")
                .setFooter("System time | " + Bot.getBotTime(), null)
                .setColor(Chat.CUSTOM_RED).build()));
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {

        ChannelLogger.logGuildMessage(event.getGuild(), new MessageBuilder().setEmbed(Chat.getEmbed().setTitle(Chat.getFullName(event.getUser()), null)
                .setDescription("*was banned from the server.*")
                .setFooter("System time | " + Bot.getBotTime(), null)
                .setColor(Chat.CUSTOM_RED).build()));
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {

        ChannelLogger.logGuildMessage(event.getGuild(), new MessageBuilder().setEmbed(Chat.getEmbed().setTitle(Chat.getFullName(event.getUser()), null)
                .setDescription("*was unbanned from the server.*")
                .setFooter("System time | " + Bot.getBotTime(), null)
                .setColor(Chat.CUSTOM_PURPLE).build()));
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embed = Chat.getEmbed();
        StringBuilder roles = new StringBuilder();

        if (GuildManager.getGuildConfig(guild).getMutedRoleId() != null
                && event.getRoles().contains(guild.getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId()))) {
            Role mutedRole = Dragon.getClient().getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId());

            for (Role role : event.getRoles()) {
                if (role != mutedRole) roles.append("`").append(role.getName()).append("`, ");
            }
        } else {
            for (Role role : event.getRoles()) {
                roles.append("`").append(role.getName()).append("`, ");
            }
        }

        if (roles.length() > 0) {
            embed.setDescription(event.getMember().getAsMention() + " gained the role(s):\n" + roles.substring(0, roles.length() - 2));

            ChannelLogger.logGuildMessage(guild, new MessageBuilder().setEmbed(embed.setFooter("System time | " + Bot.getBotTime(), null)
                    .setColor(Chat.CUSTOM_ORANGE).build()));
        }
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        Guild guild = event.getGuild();
        EmbedBuilder embed = Chat.getEmbed();
        StringBuilder roles = new StringBuilder();

        if (guild.getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId()) != null &&
                event.getRoles().contains(Dragon.getClient().getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId()))) {
            Role mutedRole = Dragon.getClient().getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId());

            for (Role role : event.getRoles()) {
                if (role != mutedRole) roles.append("`").append(role.getName()).append("`, ");
            }
        } else {
            for (Role role : event.getRoles()) {
                roles.append("`").append(role.getName()).append("`, ");
            }
        }

        if (roles.length() > 0) {
            embed.setDescription(event.getMember().getAsMention() + " lost the role(s):\n" + roles.substring(0, roles.length() - 2));

            ChannelLogger.logGuildMessage(guild, new MessageBuilder().setEmbed(embed.setFooter("System time | " + Bot.getBotTime(), null)
                    .setColor(Chat.CUSTOM_ORANGE).build()));
        }
    }

    @Override
    public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) { //TODO Add Minecraft name ???
        User user = event.getMember().getUser();

        ChannelLogger.logGuildMessage(event.getGuild(), new MessageBuilder().setEmbed(Chat.getEmbed()
                .setDescription(Chat.getFullName(user) + " | " + event.getMember().getAsMention() + " Changed nick\n" +
                        "`" + (event.getPrevNick() == null ? "null" : event.getPrevNick()) +
                        "` -> `" + (event.getNewNick() == null ? "null" : event.getNewNick()) + "`")
                .setColor(Chat.CUSTOM_ORANGE).setFooter("System time | " + Bot.getBotTime(), null).build()));
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        Dragon.getLog().info("Connection to Discord has been reestablished!");
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        Dragon.getLog().warn("Disconnected from Discord.");
    }


}
