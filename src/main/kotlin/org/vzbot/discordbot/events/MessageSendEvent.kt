package org.vzbot.discordbot.events

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.vzbot.VzBot

const val URL_REGEX =
    "(https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|www\\.[a-zA-Z0-9][a-zA-Z0-9-]+[a-zA-Z0-9]\\.[^\\s]{2,}|https?:\\/\\/(?:www\\.|(?!www))[a-zA-Z0-9]+\\.[^\\s]{2,}|www\\.[a-zA-Z0-9]+\\.[^\\s]{2,})"

class MessageSendEvent : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.channel !is TextChannel) return

        val uChannel = event.channel as TextChannel
        val message = event.message
        val member = event.member ?: return

        if (member.user.isBot) return

        if (VzBot.configFileManager.getChannels().contains(uChannel)) {
            if (message.attachments.size == 0 && !message.contentRaw.matches(URL_REGEX.toRegex())) {
                VzBot.channelLogger.sendMessage("Deleted message ${message.contentRaw} from ${uChannel.asMention} because it did not include a media.")
                message.delete().queue()
            }
        }
    }
}
