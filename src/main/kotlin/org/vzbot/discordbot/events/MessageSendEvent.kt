package org.vzbot.discordbot.events

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.daos.MessageDAO
import org.vzbot.discordbot.daos.SubmissionDAO
import org.vzbot.discordbot.models.Message
import org.vzbot.discordbot.models.Submission
import org.vzbot.discordbot.models.messageDAO
import org.vzbot.discordbot.models.submissionDAO
import org.vzbot.discordbot.vzbot.VzBot
import java.io.File
import java.io.InputStream
import java.util.logging.ConsoleHandler

const val URL_REGEX = "[-a-zA-Z0-9@:%._\\\\+~#=]{1,256}\\\\.[a-zA-Z0-9()]{1,6}\\\\b([-a-zA-Z0-9()@:%_\\\\+.~#?&//=]*)"

class MessageSendEvent: ListenerAdapter() {


    override fun onMessageReceived(event: MessageReceivedEvent) {

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

        if (uChannel is ThreadChannel) {
            val channel = uChannel as ThreadChannel
            if (!channel.name.startsWith("Submission ")) return
            val id = channel.name.substring(11)

            if (id.toLongOrNull() == null) return

            if (!submissionDAO.hasSubmission(member.idLong)) return message.delete().queue()

            var owner = submissionDAO.getSubmissionID(id.toLong())

            if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                if (member.idLong != owner.memberID) {
                    message.delete().queue()
                    return
                }
            }

            val tronxy = event.guild.id == "810876385848852510"

            val submissionChannelTronxy = event.jda.getGuildById(810876385848852510)!!.getTextChannelById(VzBot.configFileManager.getTronxySubmissionChannel()) ?: error("Invalid submissionchannel in config given")
            val submissionChannelVZ = event.jda.getGuildById(829828765512106054)!!.getTextChannelById(VzBot.configFileManager.getVZSubmissionChannel()) ?: error("Invalid submissionchannel in config given")
            val oSubmissionChannel = if (!tronxy) submissionChannelTronxy else submissionChannelVZ

            val channels = oSubmissionChannel.threadChannels.toList().toMutableList()
            channels += oSubmissionChannel.retrieveArchivedPublicThreadChannels().complete()

            val thread = channels.first { it.name == channel.name }

            val msg = thread.sendMessage(message.contentRaw.ifEmpty { "File: " })
            for (att in message.attachments) {
                msg.addFile(att.downloadToFile().get())
            }
            val messageO = Message()
            val msgSent = msg.complete()


            messageO.id = message.idLong
            messageO.channel = channel.idLong
            messageO.oID = msgSent.idLong
            messageO.oChannel = thread.idLong

            messageDAO.create(messageO)
        }

    }
}