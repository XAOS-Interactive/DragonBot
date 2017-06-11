package com.xaosia.dragonbot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.arsenarsen.lavaplayerbridge.player.Track;
import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.commands.management.ToggleMusicCommand;
import com.xaosia.dragonbot.guilds.GuildConfig;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.music.VideoThread;
import net.dv8tion.jda.core.entities.*;

public class PlayCommand extends Command {

    public PlayCommand() {
        super("play", CommandType.MUSIC, "<term>");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message);

        Player player = Dragon.getMusicManager().getPlayer(guild.getId());

        if (!ToggleMusicCommand.canQueue.get(guild.getId()) && !GuildManager.getGuildConfig(guild).isTrusted(sender)) {
            Chat.sendMessage(sender.getAsMention() + " Music commands are currently disabled. " +
                    "If you believe this is an error, please contact a staff member", channel, 10);
            return true;
        }

        if (args.length == 0) {
            return false;
        } else if (args.length >= 1) {
            if (guild.getSelfMember().getVoiceState().getChannel() == null) {
                 //rejoin the voice channel
                try {
                    VoiceChannel voiceChannel = Dragon.getClient().getVoiceChannelById(GuildManager.getGuildConfig(guild).getMusicChannelId());
                    if (voiceChannel != null) {
                        voiceChannel.getGuild().getAudioManager().openAudioConnection(voiceChannel);
                    }
                } catch (Exception ex) {
                    Chat.sendMessage(sender.getAsMention() + " The bot is not in a voice channel!", channel, 10);
                    return true;
                }
            }
            if (!guild.getSelfMember().getVoiceState().getChannel().equals(sender.getVoiceState().getChannel())) {
                Chat.sendMessage(sender.getAsMention() + " You must be in the music channel in order to play songs!", channel, 10);
                return true;
            }

            if (args[0].startsWith("http") || args[0].startsWith("www.")) {
                if (!GuildManager.getGuildConfig(guild).isTrusted(sender)) {
                    if (!player.getPlaylist().isEmpty()) {
                        for (Track track : player.getPlaylist()) {
                            if (track.getTrack().getInfo().uri.equals(args[0])) {
                                Chat.sendMessage(sender.getAsMention() + " That song is already in the playlist!", channel, 10);
                                return true;
                            }
                        }

                        int tracks = 0;
                        for (Track track : player.getPlaylist()) {
                            if (sender.getUser().getId().equals(track.getMeta().get("requester").toString())) {
                                if (tracks >= 3) {
                                    Chat.sendMessage(sender.getAsMention() + " You have already queued the max of **4** songs in the playlist! Please try again later", channel, 10);
                                    return true;
                                }
                                tracks++;
                            }
                        }

                    }
                    if (player.getPlayingTrack() != null && player.getPlayingTrack().getTrack().getInfo().uri.equals(args[0])) {
                        Chat.sendMessage(sender.getAsMention() + " That song is already in the playing!", channel, 10);
                        return true;
                    }
                }
                VideoThread.getThread(args[0], channel, sender).start();
            } else {
                StringBuilder term = new StringBuilder();
                for (String s : args) {
                    term.append(s).append(" ");
                }
                term = new StringBuilder(term.toString().trim());
                VideoThread.getSearchThread(term.toString(), channel, sender).start(); //YouTube only
            }

        }

        return true;
    }

}
