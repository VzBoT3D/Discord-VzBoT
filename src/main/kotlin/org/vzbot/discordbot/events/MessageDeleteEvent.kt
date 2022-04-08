package org.vzbot.discordbot.events

import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.daos.MessageDAO
import org.vzbot.discordbot.models.messageDAO
import org.vzbot.discordbot.models.submissionDAO
import org.vzbot.discordbot.vzbot.VzBot

class MessageDeleteEvent: ListenerAdapter() {

    override fun onMessageDelete(event: MessageDeleteEvent) {

        val channel = event.channel
        val msg = event.messageIdLong

        if (channel is ThreadChannel) {
            val id = channel.name.substring(11)
            if (id.toLongOrNull() == null) return

            val idLong = id.toLong()

            if (!submissionDAO.hasSubmissionID(idLong)) return
            if (!messageDAO.hasID(msg) && !messageDAO.hasOID(msg)) return

            val message = messageDAO.getIdOrOID(msg)

            val messageToDeleteID = if (message.oID == msg) message.id else message.oID

            val tronxy = event.guild.id == "810876385848852510"

            val submissionChannelTronxy = event.jda.getGuildById(810876385848852510)!!.getTextChannelById(VzBot.configFileManager.getTronxySubmissionChannel()) ?: error("Invalid submissionchannel in config given")
            val submissionChannelVZ = event.jda.getGuildById(829828765512106054)!!.getTextChannelById(VzBot.configFileManager.getVZSubmissionChannel()) ?: error("Invalid submissionchannel in config given")
            val oSubmissionChannel = if (!tronxy) submissionChannelTronxy else submissionChannelVZ

            val channelID = if (channel.idLong == message.channel) message.oChannel else message.channel

            val channels = oSubmissionChannel.threadChannels.toList().toMutableList()
            channels += oSubmissionChannel.retrieveArchivedPublicThreadChannels().complete()

            val thread = channels.first { it.idLong == channelID }

            thread!!.deleteMessageById(messageToDeleteID).queue()
            messageDAO.delete(message)
        }
    }
}