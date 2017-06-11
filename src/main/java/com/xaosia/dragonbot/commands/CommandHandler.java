package com.xaosia.dragonbot.commands;

import com.xaosia.dragonbot.exceptions.CommandException;
import com.xaosia.dragonbot.utils.Bot;
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

        if (message.getRawContent() != null && message.getContent().startsWith(Dragon.getConfig().getCommandPrefix())) {
            if (Dragon.getConfig().getBlockedUsers().contains(sender.getUser().getId()) && !Dragon.getConfig().isTrusted(sender)) {
                Chat.removeMessage(message);
                sender.getUser().openPrivateChannel().queue(c ->
                        c.sendMessage("You are blacklisted from using bot commands. " +
                                "If you believe this is an error, please contact spacetrain31.").queue(m -> m.getPrivateChannel().close()));
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

            if (cmd.getType() == Command.CommandType.MASTER && !sender.getUser().equals(Bot.getCreator())) {
                return;
            }

            if (cmd.getType() == Command.CommandType.TRUSTED && !Dragon.getConfig().isTrusted(sender)) {
                return;
            }

            if (cmd.getType() == Command.CommandType.MUSIC && !Dragon.getConfig().getMusicCommandChannels().contains(channel.getId())) {
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
        //cmds.add(new MemeCommand());

        //management


        //master
        //cmds.add(new DisableCommand(this));
        //cmds.add(new EnableCommand(this));
        //cmds.add(new ReloadCommand());

        //music
        //todo: redo music commands
    }


}
