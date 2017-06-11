package com.xaosia.dragonbot.utils;

import com.xaosia.dragonbot.Dragon;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class IDiscordClient {

    public boolean isReady() {
        return Dragon.getClient().getStatus().equals(JDA.Status.CONNECTED);
    }

    public List<VoiceChannel> getConnectedVoiceChannels() {
        return Dragon.getClient().getGuilds().stream()
                .map(c -> c.getAudioManager().getConnectedChannel())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long getActiveVoiceChannels() {
        return getConnectedVoiceChannels().stream()
                .map(VoiceChannel::getGuild)
                .map(ISnowflake::getId)
                .filter(gid -> Dragon.getMusicManager().hasPlayer(gid))
                .map(g -> Dragon.getMusicManager().getPlayer(g))
                .filter(p -> p.getPlayingTrack() != null)
                .filter(p -> !p.getPaused()).count();
    }

    public List<Guild> getRolesForGuild(Guild guild) {
        return Dragon.getClient().getGuilds().stream().filter(g -> g.getRoles().equals(guild)).collect(Collectors.toList());
    }

    public boolean userHasRoleId(Member member, String id) {
        return member.getRoles().contains(Dragon.getClient().getRoleById(id));
    }

    public void streaming(String status, String url) {
        Dragon.getClient().getPresence().setGame(Game.of(status, url));
    }

}
