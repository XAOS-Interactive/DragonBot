package com.xaosia.dragonbot.music.extractors;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;


public interface Extractor {
    Class<? extends AudioSourceManager> getSourceManagerClass();

    void process(String input, Player player, Message message, Guild guild, Member member) throws Exception;

    boolean valid(String input);
}
