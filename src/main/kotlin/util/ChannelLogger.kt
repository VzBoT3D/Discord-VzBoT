package util

import net.dv8tion.jda.api.entities.TextChannel
import vzbot.VzBot

class ChannelLogger(channelID: String) {

    private var channel: TextChannel = VzBot.discord.getTextChannelById(channelID) ?: error("Channel for Logging was not found")

    fun sendMessage(message: String) {
        println(message)
        channel.sendMessageEmbeds(defaultEmbed(message)).queue()
    }

}