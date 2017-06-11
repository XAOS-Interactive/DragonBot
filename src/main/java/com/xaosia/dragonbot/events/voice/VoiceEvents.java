package com.xaosia.dragonbot.events.voice;

import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VoiceEvents extends ListenerAdapter {

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            event.getGuild().getAudioManager().setSelfDeafened(true);
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getUser().equals(event.getJDA().getSelfUser())) {
            if (Dragon.getMusicManager().hasPlayer(event.getGuild().getId())) {
                Dragon.getMusicManager().getPlayer(event.getGuild().getId()).getPlaylist().clear();
                Dragon.getMusicManager().getPlayer(event.getGuild().getId()).skip();
            }
        } else {
            if (event.getChannelLeft().getMembers().contains(event.getGuild().getMember(event.getJDA().getSelfUser())) && event.getChannelLeft().getMembers().size() < 2) {
                event.getChannelLeft().getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

}
