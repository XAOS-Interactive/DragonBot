package com.xaosia.dragonbot.commands.music;

import com.arsenarsen.lavaplayerbridge.player.Player;
import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.exceptions.CommandException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;


public class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        super("np", CommandType.MUSIC, "", "nowplaying", "current", "song", "playing");
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message, 5);

        Player player = Dragon.getMusicManager().getPlayer(guild.getId());

        if (Dragon.getMusicManager().getPlayer(guild.getId()).getPlayingTrack() != null) {
            Chat.sendMessage(Chat.getEmbed().addField("Currently Playing", String.format("**[%s](%s)** `[%s]` | <@!%s>",
                    player.getPlayingTrack().getTrack().getInfo().title, player.getPlayingTrack().getTrack().getInfo().uri,
                    Bot.millisToTime(player.getPlayingTrack().getTrack().getDuration(), false), player.getPlayingTrack().getMeta().get("requester")), false)
                    .addField("Volume", player.getVolume() + "%", true)
                    .addField("Repeating", player.getLooping() ? ":white_check_mark:" : ":x:", true)
                    .addField("Paused", player.getPaused() ? ":white_check_mark:" : ":x:", true).build(), channel, 25);
        } else {
            Chat.sendMessage(Chat.getEmbed().setDescription("There are no songs playing!").setColor(Chat.CUSTOM_RED).build(), channel, 20);
        }

        return true;
    }

}
