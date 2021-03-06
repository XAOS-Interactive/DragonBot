package com.xaosia.dragonbot.commands.music;

import com.xaosia.dragonbot.commands.Command;
import com.xaosia.dragonbot.utils.Chat;
import com.xaosia.dragonbot.exceptions.CommandException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class VolumeCommand extends Command {

    public VolumeCommand() {
        super("volume", CommandType.MUSIC, null, "vol", "sound");
        setEnabled(false);
    }

    @Override
    public boolean onCommand(Guild guild, TextChannel channel, Member sender, Message message, String[] args) throws CommandException {
        Chat.removeMessage(message, 5);

        Chat.sendMessage(sender.getAsMention() + "\nSince that command was overloading the CPU, is has been removed.\n" +
                "If you want to change the volume, do this instead: https://gfycat.com/UnrulyBountifulAfricancivet\n**(10-15% recommended)**", channel, 30);

        return true;
    }

}
