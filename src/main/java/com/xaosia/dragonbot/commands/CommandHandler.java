package com.xaosia.dragonbot.commands;

import com.xaosia.dragonbot.commands.general.MemeCommand;
import com.xaosia.dragonbot.commands.general.uhcfriends.SeniorsCommand;
import com.xaosia.dragonbot.commands.management.*;
import com.xaosia.dragonbot.commands.master.DisableCommand;
import com.xaosia.dragonbot.commands.master.EnableCommand;
import com.xaosia.dragonbot.commands.master.ReloadCommand;
import com.xaosia.dragonbot.commands.music.*;
import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.PermissionsUtil;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import com.xaosia.dragonbot.Dragon;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler extends ListenerAdapter {

    private static List<Command> cmds = new ArrayList<>();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        Message message = event.getMessage();
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        User user = message.getAuthor();
        TextChannel channel = event.getChannel();


        if (message.getRawContent() != null && (message.getContent().startsWith(GuildManager.getGuildConfig(guild).getPrefix()) ||
                message.getContent().startsWith(Dragon.getConfig().getCommandPrefix()))) {
            if (Dragon.getConfig().getBlockedUsers().contains(sender.getUser().getId())) {
                Chat.removeMessage(message);
                Chat.sendPM("You are blacklisted from using bot commands. " +
                        "If you believe this is an error, please contact spacetrain31.", sender.getUser());
                return;
            }

            //make sure the bot is setup
            if (!GuildManager.getGuildConfig(guild).isSetup() && (!PermissionsUtil.hasAdministrator(sender) ||
                    !PermissionsUtil.isBotCreator(sender.getUser()))) {

                Chat.sendMessage(sender.getAsMention() + "I have not been setup yet, I will not function properly until I have been setup, " +
                        "a server owner has to first set me up.", channel);

                return;
            }

            String msg = event.getMessage().getRawContent();
            String command = msg.substring(1);
            String[] args = new String[0];
            if (msg.contains(" ")) {
                command = command.substring(0, msg.indexOf(" ") - 1);
                args = msg.substring(msg.indexOf(" ") + 1).split(" ");
            }

            Command cmd = getCommand(command);

            if (cmd == null) return; //invalid command

            if (cmd.getType() == Command.CommandType.MASTER && !PermissionsUtil.isBotCreator(sender.getUser())) {
                return;
            }

            if (cmd.getType() == Command.CommandType.TRUSTED && !PermissionsUtil.isTrusted(guild, sender)) {
                return;
            }

            if (cmd.getType() == Command.CommandType.MUSIC && !GuildManager.getGuildConfig(guild).getMusicChannelId().equals(channel.getId())) {
                return;
            }

            if (!cmd.isEnabled()) {
                //Command is not enabled, send user a message informing them
                sender.getUser().openPrivateChannel().queue(c -> c.sendMessage("The command " + cmd.getName() + " is currently disabled.").queue(m -> m.getPrivateChannel().close()));
                return;
            }

            try {
                if (!cmd.onCommand(guild, channel, sender, message, args)) {
                    Chat.sendMessage(sender.getAsMention() + " Usage: ```" + Dragon.getConfig().getCommandPrefix() + cmd.getUsage() + "```", channel, 20);
                }
            } catch (CommandException e) {
                Dragon.getLog().error(e.getMessage(), e);
            }

        }

    }

    /**
     * Get a command.
     *
     * @param name The name of the command
     * @return The Command if found, null otherwise.
     */
    public Command getCommand(String name) {
        for (Command cmd : cmds) {
            if (cmd.getAliases().length > 0) {
                for (String alias : cmd.getAliases()) {
                    if (cmd.getName().equalsIgnoreCase(name) || alias.equalsIgnoreCase(name)) {
                        return cmd;
                    }
                }
            } else {
                if (cmd.getName().equalsIgnoreCase(name)) {
                    return cmd;
                }
            }
        }
        return null;
    }

    public void registerCommands() {

        //general
        cmds.add(new MemeCommand());
        cmds.add(new SeniorsCommand());

        //management
        cmds.add(new AddTrustedCommand());
        cmds.add(new SetCmdCommand());
        cmds.add(new SetLogCommand());
        cmds.add(new SetMusicCommand());
        cmds.add(new ToggleMusicCommand());

        //master
        cmds.add(new DisableCommand(this));
        cmds.add(new EnableCommand(this));
        cmds.add(new ReloadCommand());

        //music
        cmds.add(new NowPlayingCommand());
        cmds.add(new PlayCommand());
        cmds.add(new QueueCommand());
        cmds.add(new RandomCommand());
        cmds.add(new SkipCommand());
        cmds.add(new VolumeCommand());
    }


}
