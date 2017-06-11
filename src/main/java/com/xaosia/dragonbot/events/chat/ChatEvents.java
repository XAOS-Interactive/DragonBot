package com.xaosia.dragonbot.events.chat;


import com.xaosia.dragonbot.Dragon;
import com.xaosia.dragonbot.guilds.GuildManager;
import com.xaosia.dragonbot.utils.Bot;
import com.xaosia.dragonbot.utils.ChannelLogger;
import com.xaosia.dragonbot.utils.Chat;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.HashMap;

public class ChatEvents extends ListenerAdapter {

    public static HashMap<String, String> messages;
    public static HashMap<String, Integer> amount;

    private static void filterMessage(Message message) {
        Guild guild = message.getGuild();
        User user = message.getAuthor();
        TextChannel channel = message.getTextChannel();

        if (!GuildManager.getGuildConfig(guild).isTrusted(guild.getMember(user))) {
            if (Bot.hasInvite(message)) {
                Chat.removeMessage(message);

                channel.sendMessage(user.getAsMention() + " Please do not advertise Discord servers. Thanks!").queue();
                EmbedBuilder builder = Chat.getEmbed().setDescription(":exclamation: Discord server advertisement - **" + Chat.getFullName(user) + "**")
                        .addField("User", user.getAsMention(), true)
                        .addField("Channel", channel.getAsMention(), true);
                if (message.getContent().length() >= 1024) {
                    builder.addField("Message", "```" + message.getContent().substring(0, 1014).replace("`", "\\`") + "...```", false);
                } else {
                    builder.addField("Message", "```" + message.getContent().replace("`", "\\`") + "```", false);
                }

                ChannelLogger.logGuildMessage(guild,
                        new MessageBuilder().setEmbed(builder.setFooter(Bot.getBotTime(), null).setColor(Chat.CUSTOM_PURPLE).build()));
                return;
            }

            if (!channel.getName().contains("meme") && message.getRawContent().contains("▔╲▂▂▂▂╱▔╲▂")) {
                Chat.removeMessage(message);
                Chat.sendMessage(user.getAsMention() + " Please do not post cooldog in this channel.", channel, 30);
            }
        }
    }

    @Override
    public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
        if (event.getGuild() == null || !Dragon.getDiscord().isReady() || event.getMember().getUser().equals(Dragon.getClient().getSelfUser()) ||
                event.getMember().getUser().isBot()) {
            return;
        }

        filterMessage(event.getMessage());
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        User user = message.getAuthor();
        TextChannel channel = event.getChannel();

        if (guild == null || !Dragon.getDiscord().isReady() || user.equals(Dragon.getClient().getSelfUser()) || user.isBot()) {
            return;
        }

        if (!GuildManager.getGuildConfig(guild).isTrusted(sender) && (!message.getContent().startsWith(GuildManager.getGuildConfig(guild).getPrefix()) ||
            !message.getContent().startsWith(Dragon.getConfig().getCommandPrefix()))) {
            if (GuildManager.getGuildConfig(guild).isFilterSpamEnabled()) {
                if (message.getContent().length() >= Dragon.getConfig().getMaxMessageLength()) {
                    EmbedBuilder builder = Chat.getEmbed().setDescription(":exclamation: Possible message spam - **" + Chat.getFullName(user) + "**")
                            .addField("User", user.getAsMention(), true)
                            .addField("Length", String.valueOf(message.getContent().length()), true)
                            .addField("Channel", channel.getAsMention(), true);
                    if (message.getContent().length() >= 1024) {
                        builder.addField("Message", "```" + message.getContent().replace("`", "\\`").substring(0, 1014) + "...```", false);
                    } else {
                        builder.addField("Message", "```" + message.getContent().replace("`", "\\`") + "```", false);
                    }

                    ChannelLogger.logGuildMessage(guild, new MessageBuilder().setEmbed(builder.setFooter("System time | " + Bot.getBotTime(), null).setColor(Chat.CUSTOM_PURPLE).build()));
                }

                if (GuildManager.getGuildConfig(guild).getMutedRoleId() != null) {
                    if (message.getRawContent().equalsIgnoreCase(messages.get(user.getId()))) {
                        amount.put(user.getId(), (amount.get(user.getId()) + 1));
                    } else {
                        messages.put(user.getId(), message.getRawContent());
                        amount.put(user.getId(), 0);
                    }

                    if (amount.get(user.getId()) != null && ((amount.get(user.getId()) + 1) == 3 || (amount.get(user.getId()) + 1) == 4)) {
                        Chat.removeMessage(event.getMessage());
                        channel.sendMessage(user.getAsMention() + " Please do not repeat the same message!").queue();
                        return;
                    } else if ((amount.get(user.getId()) + 1) >= 5) {
                        Chat.removeMessage(event.getMessage());

                        guild.getController().addRolesToMember(sender, Dragon.getClient().getGuildById(guild.getId()).getRoleById(GuildManager.getGuildConfig(guild).getMutedRoleId())).queue();
                        channel.sendMessage(user.getAsMention() + " has been auto muted for spam").queue();

                        EmbedBuilder builder = Chat.getEmbed().setDescription(":no_bell:  " + user.getAsMention() + " | " + Chat.getFullName(user) + " was auto muted for spam!")
                                .addField("Channel", channel.getAsMention(), true);
                        if (event.getMessage().getContent().length() >= 1024) {
                            builder.addField("Message", "```" + event.getMessage().getContent().substring(0, 1014).replace("`", "\\`") + "...```", false);
                        } else {
                            builder.addField("Message", "```" + event.getMessage().getContent().replace("`", "\\`") + "```", false);
                        }

                        ChannelLogger.logGuildMessage(guild, new MessageBuilder().setEmbed(builder.setFooter("System time | " + Bot.getBotTime(), null).setColor(Chat.CUSTOM_PURPLE).build()));
                        return;
                    }
                }

                filterMessage(message);
            }
        }
    }

}
