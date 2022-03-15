package org.vzbot.discordbot.events

import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.daos.SubmissionDAO
import org.vzbot.discordbot.models.Submission
import org.vzbot.discordbot.models.submissionDAO
import org.vzbot.discordbot.vzbot.VzBot

class MessageSendEvent: ListenerAdapter() {


    override fun onMessageReceived(event: MessageReceivedEvent) {

        val channel = event.channel
        val message = event.message
        if (VzBot.configFileManager.getChannels().contains(channel)) {
            if (message.attachments.size == 0) {
                message.delete().queue()
                VzBot.channelLogger.sendMessage("Deleted message ${message.id} from ${channel.asMention} because it did not include a media.")
            }
        }

        if (channel is ThreadChannel) {
            val id = channel.name

            if (id.toLongOrNull() == null) return
            var owner = submissionDAO.getSubmissionID(id.toLong())
        }

    }


}