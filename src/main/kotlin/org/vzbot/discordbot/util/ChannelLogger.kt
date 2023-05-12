package org.vzbot.discordbot.util

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

class ChannelLogger(channelID: String) {

    private var channel: TextChannel =
        VzBot.discord.getTextChannelById(channelID) ?: error("Channel for Logging was not found")

    fun sendMessage(message: String) {
        channel.sendMessageEmbeds(defaultEmbed(message, Color.ORANGE, "Info")).queue()
    }

    fun sendError(message: String) {
        channel.sendMessageEmbeds(defaultEmbed(message, Color.RED, "Error")).queue()
    }

    fun sendSuccess(message: String) {
        channel.sendMessageEmbeds(defaultEmbed(message, Color.GREEN, "Success")).queue()
    }
}
