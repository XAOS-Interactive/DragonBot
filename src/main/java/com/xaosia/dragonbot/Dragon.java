package com.xaosia.dragonbot;


import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.xaosia.dragonbot.commands.CommandHandler;
import com.xaosia.dragonbot.commands.music.SkipCommand;
import com.xaosia.dragonbot.events.chat.ChatEvents;
import com.xaosia.dragonbot.events.server.ServerEvents;
import com.xaosia.dragonbot.events.voice.VoiceEvents;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.*;
import net.dv8tion.jda.core.*;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.requests.RestAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class Dragon {

    private static Dragon instance;
    private static boolean enabled = false;

    private static Config config;
    private static IDiscordClient discord;
    private static JDA client;
    private static CountDownLatch latch;
    private static PlayerManager musicManager;
    private static CommandHandler commandHandler;
    private static Logger log = LoggerFactory.getLogger("Dragon");
    private final static List<ListenerAdapter> listeners = new ArrayList<>();
    private final static File guildsDir = new File("guilds");


    public static void main(String[] args) throws InterruptedException, UnknownBindingException {
        LoggerAdapter.set();

        //load our bot config
        loadConfig();

        new Dragon().init();
        Scanner scanner = new Scanner(System.in);

        do {
            try {
                if (scanner.next().equalsIgnoreCase("exit")) {
                    shutdown(false);
                } else if (scanner.next().equalsIgnoreCase("restart")) {
                    shutdown(true);
                }
            } catch (NoSuchElementException ignored) {
            }
        } while (enabled);

    }

    private void init() throws InterruptedException, UnknownBindingException {
        instance = this;

        RestAction.DEFAULT_FAILURE = t -> {};

        discord = new IDiscordClient();
        latch = new CountDownLatch(1);
        commandHandler = new CommandHandler();

        try {

            try {
                //add .addEventListener(new ChatEvents(), new ServerEvents(), commandHandler)
                client = new JDABuilder(AccountType.BOT)
                        .setToken(config.getDiscordToken())
                        .setAudioSendFactory(new NativeAudioSendFactory())
                        .setGame(Game.of("loading..."))
                        .setStatus(OnlineStatus.IDLE)
                        .buildAsync();

                //register listeners
                addListener(commandHandler);
                addListener(new ChatEvents());
                addListener(new ServerEvents());
                addListener(new VoiceEvents());

                musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(client));
                registerMusicManager();
            } catch (RateLimitedException e) {
                Thread.sleep(e.getRetryAfter());
            }

        } catch (LoginException e) {
            log.error("Could not login to Discord!", e);
            Thread.sleep(500);
            shutdown(false);
        }

        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        })); // No operation STDERR. Will not do much of anything, except to filter out some Jsoup spam

        latch.await();
        commandHandler.registerCommands();
    }

    /**
     * Loads the config for Dragon and creates any needed directories
     */
    private static void loadConfig() {

        //load our config
        config = new Config();

        if (!config.getSettingsFile().exists()) {
            try {
                config.getSettingsFile().createNewFile();
                config.save();
                log.info("New config generated! Please enter the settings and try again");
            } catch (IOException ex) {
                log.info("Failed to create new settings.json file", ex);
                shutdown(false);
            }
        }

        try {
            log.info("Loading config...");
            config.load();
        } catch (IOException ex) {
            log.info("Failed to load config", ex);
            shutdown(false);
        }


        if (!guildsDir.exists()) {
            log.info("Creating Guilds Config Directory...");
            guildsDir.mkdir();
        }

    }

    public static void shutdown(boolean restart) {

        if (enabled) {
            enabled = false;

            getClient().getPresence().setGame(Game.of("shutting down..."));
            getClient().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);

            try {

                shutdownMusicManager();

                //TODO Remove all messages that are waiting on task timers

                client.removeEventListener(new ChatEvents(), new ServerEvents());

                if (restart) {
                    log.info("Restarting...");
                    new ProcessBuilder("/bin/bash", "run.sh").start();
                } else {
                    log.info("Cleaning things up...");
                }

                Thread.sleep(1500);

                client.shutdown();
                log.info("Client shutdown");
                System.exit(0);

            } catch (IOException e) {
                log.warn("Could not start restart process!", e);
            } catch (InterruptedException e) {
                log.warn("Could not pause shutdown thread!", e);
                e.printStackTrace();
            }

        } else {
            System.exit(0);
        }

    }

    /**
     * Registers the listener
     *
     * @param listener the listener to register
     */
    public static void addListener(ListenerAdapter listener) {
        listeners.add(listener);
        client.addEventListener(listener);
    }

    private static void registerListeners() {
        for (ListenerAdapter listener : listeners) {
            client.addEventListener(listener);
        }
    }

    /**
     * Removes all listeners currently registered.
     */
    private static void removeListeners() {
        for (ListenerAdapter listener : listeners) {
            client.removeEventListener(listener);
        }
    }

    private static void shutdownMusicManager() {
        if (!Bot.nowPlaying.isEmpty()) {
            if (!SkipCommand.votes.isEmpty()) {
                SkipCommand.votes.clear();
            }
        }

        if (discord.getConnectedVoiceChannels().size() > 0) {
            for (VoiceChannel voiceChannel : discord.getConnectedVoiceChannels()) {
                getMusicManager().getPlayer(voiceChannel.getGuild().getId()).getPlaylist().clear();
                getMusicManager().getPlayer(voiceChannel.getGuild().getId()).skip();
            }

            Bot.nowPlaying.forEach(Chat::removeMessage);
        }
    }

    public static void registerMusicManager() {
        musicManager.getPlayerCreateHooks().register(player -> player.addEventListener(new AudioEventAdapter() {
            @Override
            public void onTrackStart(AudioPlayer aplayer, AudioTrack atrack) {
                Guild guild = client.getGuildById(player.getGuildId());
                String id = GuildManager.getGuildConfig(guild).getCommandId();
                    if (id != null) {
                        TextChannel channel = client.getTextChannelById(id);
                        if (channel != null) {
                            AudioPlayer song = getMusicManager().getPlayer(channel.getGuild().getId()).getPlayer();
                            User user = getClient().getUserById(player.getPlayingTrack().getMeta().get("requester").toString());

                            if (song == aplayer || song.getPlayingTrack() == atrack) {
                                EmbedBuilder embed = Chat.getEmbed();

                                if (atrack instanceof YoutubeAudioTrack) {
                                    embed.addField("**Now playing** - YouTube", "**[" + atrack.getInfo().title + "](" + atrack.getInfo().uri + ")** " +
                                            "`[" + Bot.millisToTime(song.getPlayingTrack().getDuration(), false) + "]`", true)
                                            .setImage("https://img.youtube.com/vi/" + song.getPlayingTrack().getIdentifier() + "/mqdefault.jpg")
                                            .setFooter("Queued by: @" + Chat.getFullName(user), null)
                                            .setColor(Chat.CUSTOM_GREEN);
                                } else if (atrack instanceof SoundCloudAudioTrack) {
                                    embed.addField("**Now playing** - SoundCloud", "**[" + atrack.getInfo().title + "](" + atrack.getInfo().uri + ")** " +
                                            "`[" + Bot.millisToTime(song.getPlayingTrack().getDuration(), false) + "]`", true)
                                            .setImage("https://cdn.discordapp.com/attachments/233737506955329538/290302284381028352/soundcloud_icon.png") //I made this -Matrix
                                            .setFooter("Queued by: @" + Chat.getFullName(user), null)
                                            .setColor(Chat.CUSTOM_DARK_ORANGE);
                                } else {
                                    embed.addField("**Now playing** - ???", "**[" + atrack.getInfo().title + "](" + atrack.getInfo().uri + ")** " +
                                            "`[" + Bot.millisToTime(song.getPlayingTrack().getDuration(), false) + "]`", true)
                                            .setFooter("Queued by: @" + Chat.getFullName(user), null)
                                            .setColor(Chat.CUSTOM_GREEN);
                                }

                                Message msg = channel.sendMessage(new MessageBuilder().setEmbed(embed.build()).build()).complete();

                                SkipCommand.votes.clear();
                                Bot.nowPlaying.add(msg);
                            }
                        }
                    }
            }

            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                SkipCommand.votes.clear();

                for (Message msg : Bot.nowPlaying) {
                    if (Bot.nowPlaying.isEmpty()) {
                        break;
                    }

                    if (msg != null) {
                        AudioPlayer guildPlayer = getMusicManager().getPlayer(msg.getGuild().getId()).getPlayer();

                        if (guildPlayer == player) {
                            Chat.removeMessage(msg);
                            Bot.nowPlaying.remove(msg);
                            break;
                        }
                    }
                }
            }
        }));
    }

    public static Dragon get() {
        return instance;
    }

    public static JDA getClient() {
        return client;
    }

    public static Config getConfig() {
        return config;
    }

    public static IDiscordClient getDiscord() {
        return discord;
    }

    public static PlayerManager getMusicManager() {
        return musicManager;
    }

    public static CountDownLatch getLatch() {
        return latch;
    }

    public static Logger getLog() {
        return log;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enable) {
        enabled = enable;
    }

    public static File getGuildsDir() {
        return guildsDir;
    }

}
