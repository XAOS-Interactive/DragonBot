package com.xaosia.dragonbot.music;

import com.arsenarsen.lavaplayerbridge.PlayerManager;
import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.music.extractors.Extractor;
import com.xaosia.dragonbot.music.extractors.SoundCloudExtractor;
import com.xaosia.dragonbot.music.extractors.YouTubeExtractor;
import com.xaosia.dragonbot.music.extractors.YouTubeSearchExtractor;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VideoThread extends Thread {

    private static PlayerManager manager;
    private static final List<Class<? extends Extractor>> extractors = Arrays.asList(YouTubeExtractor.class, SoundCloudExtractor.class);
    private static final Set<Class<? extends AudioSourceManager>> managers = new HashSet<>();
    public static final ThreadGroup VIDEO_THREADS = new ThreadGroup("Video Threads");
    private Member member;
    private Guild guild;
    private TextChannel channel;
    private String url;
    private Extractor extractor;

    private VideoThread() {
        if (manager == null)
            manager = Dragon.getMusicManager();
        setName("Video Thread " + VIDEO_THREADS.activeCount());
    }

    @Override
    public void run() {
        Message message = channel.sendMessage("Processing...").complete(); //TODO Add editing messages from different commands (random)
        Chat.removeMessage(message, 120);
        try {
            if (extractor == null)
                for (Class<? extends Extractor> clazz : extractors) {
                    Extractor extractor = clazz.newInstance();
                    if (!extractor.valid(url))
                        continue;
                    this.extractor = extractor;
                    break;
                }
            if (extractor == null) {
                Chat.editMessage(member.getAsMention() + " Your request could not be processed. Please try again", message, 10);
                return;
            }
            if (managers.add(extractor.getSourceManagerClass()))
                manager.getManager().registerSourceManager(extractor.getSourceManagerClass().newInstance());
            extractor.process(url, manager.getPlayer(channel.getGuild().getId()), message, channel.getGuild(), member);
        } catch (Exception e) {
            Dragon.getLog().error("Could not init extractor for '{}'".replace("{}", url), e);
            Chat.editMessage(member.getAsMention() + " Something went wrong while processing that. Please try again later", message, 10);
        }
    }

    @Override
    public void start() {
        if (url == null)
            throw new IllegalStateException("URL Was not set!");
        super.start();
    }

    public static VideoThread getThread(String url, TextChannel channel, Member member) {
        VideoThread thread = new VideoThread();
        thread.url = url;
        thread.channel = channel;
        thread.member = member;
        return thread;
    }

    public static VideoThread getSearchThread(String term, TextChannel channel, Member member) {
        VideoThread thread = new VideoThread();
        thread.url = term;
        thread.channel = channel;
        thread.member = member;
        thread.extractor = new YouTubeSearchExtractor();
        return thread;
    }
}
