package com.xaosia.dragonbot;


import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.arsenarsen.lavaplayerbridge.libraries.LibraryFactory;
import com.arsenarsen.lavaplayerbridge.libraries.UnknownBindingException;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.xaosia.dragonbot.commands.CommandHandler;
import com.xaosia.dragonbot.events.chat.ChatEvents;
import com.xaosia.dragonbot.events.server.ServerEvents;
import com.xaosia.dragonbot.utils.Config;
import com.xaosia.dragonbot.utils.IDiscordClient;
import com.xaosia.dragonbot.utils.LoggerAdapter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
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

                musicManager = PlayerManager.getPlayerManager(LibraryFactory.getLibrary(client));
                //registerEvents(); //todo: register music events
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

                //todo: shutdown the music manager

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
