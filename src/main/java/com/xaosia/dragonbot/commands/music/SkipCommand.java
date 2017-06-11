package com.xaosia.dragonbot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.commands.management.ToggleMusicCommand;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.exceptions.CommandException;
import net.dv8tion.jda.core.entities.*;

import java.util.List;

public class SkipCommand extends Command {

    public SkipCommand() {
        super("skip", CommandType.MUSIC, null, "next");
    }

    public static List<String> votes;
    private static int maxSkips = 0;

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message);

        Player player = Dragon.getMusicManager().getPlayer(guild.getId());
        VoiceChannel voiceChannel = guild.getSelfMember().getVoiceState().getChannel();

        if (!ToggleMusicCommand.canQueue.get(guild.getId()) && !GuildManager.getGuildConfig(guild).isTrusted(sender)) {
            Chat.sendMessage(sender.getAsMention() + " Music commands are currently disabled. " +
                    "If you believe this is an error, please contact a staff member", channel, 10);
            return true;
        }

        if (!guild.getAudioManager().isConnected() ||
                Dragon.getMusicManager().getPlayer(guild.getId()).getPlayingTrack() == null) {
            Chat.sendMessage("The player is not playing!", channel, 15);
            return true;
        }
        if (args.length == 1 && args[0].equals("force") && GuildManager.getGuildConfig(guild).isTrusted(sender)) {
            votes.clear();
            Chat.sendMessage(sender.getAsMention() + " Force skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 15);
            player.skip();
            return true;
        }
        if (guild.getSelfMember().getVoiceState().getChannel() == null) {
            Chat.sendMessage(sender.getAsMention() + " The bot is not in a voice channel!", channel, 10);
            return true;
        }
        if (!guild.getSelfMember().getVoiceState().getChannel().equals(sender.getVoiceState().getChannel())) {
            Chat.sendMessage(sender.getAsMention() + " you must be in the channel in order to skip songs!", channel, 10);
            return true;
        }
        if (sender.getUser() == Dragon.getClient().getUserById(player.getPlayingTrack().getMeta().get("requester").toString())) {
            votes.clear();
            Chat.sendMessage("Skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 20);
            player.skip();
            return true;
        }
        if (votes.contains(sender.getUser().getId())) {
            Chat.sendMessage(sender.getAsMention() + " you have already voted to skip this song!", channel, 10);
            return true;
        }
        votes.add(sender.getUser().getId());

        if (voiceChannel != null && maxSkips != -1) {
            if (voiceChannel.getMembers().size() > 2) {
                maxSkips = (int) ((voiceChannel.getMembers().size() - 1) * 2 / 3.0 + 0.5);
            } else {
                maxSkips = 1;
            }

            if (maxSkips - votes.size() <= 0 || maxSkips == -1) {
                votes.clear();
                Chat.sendMessage("Skipped **" + player.getPlayingTrack().getTrack().getInfo().title + "**", channel, 20);
                player.skip();
            } else {
                Chat.sendMessage(sender.getAsMention() + " voted to skip!\n **" +
                        (maxSkips - votes.size()) + "** more votes are required to skip the current song.", channel, 20);
            }
        }

        return true;
    }

}
