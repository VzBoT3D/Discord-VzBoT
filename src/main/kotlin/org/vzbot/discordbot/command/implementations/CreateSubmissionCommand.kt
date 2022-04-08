package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.models.Submission
import org.vzbot.discordbot.models.submissionDAO
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot

private val dat = CommandData("submit", "Will create your own Thread to post your submission.")
class CreateSubmissionCommand: Command("submit",dat , false) {

    override fun execute(member: Member, event: SlashCommandEvent) {

        val tronxy = event.guild!!.id == "810876385848852510"

        if (submissionDAO.hasSubmission(member.idLong)) {
            val sub = submissionDAO.getSubmission(member.idLong)
            event.replyEmbeds(defaultEmbed("You already have sent your submission- ID: ${sub.id}")).queue()
            return
        }

        val submissionChannelTronxy = event.jda.getGuildById(810876385848852510)!!.getTextChannelById(VzBot.configFileManager.getTronxySubmissionChannel()) ?: error("Invalid submissionchannel in config given")
        val submissionChannelVZ = event.jda.getGuildById(829828765512106054)!!.getTextChannelById(VzBot.configFileManager.getVZSubmissionChannel()) ?: error("Invalid submissionchannel in config given")
        val submissionChannel = if (tronxy) submissionChannelTronxy else submissionChannelVZ
        val oSubmissionChannel = if (!tronxy) submissionChannelTronxy else submissionChannelVZ

        if (event.channel.id != submissionChannel.id) {
            event.replyEmbeds(defaultEmbed("Please use the submission channel to create a new submission ${if (tronxy) submissionChannelTronxy.asMention else submissionChannelVZ.asMention}")).queue()
            return
        }


        val submissionID = submissionDAO.listAll().size + 1
        submissionChannel.createThreadChannel("Submission $submissionID").queue()
        oSubmissionChannel.createThreadChannel("Submission $submissionID").queue()

        val submission = Submission()
        submission.id = submissionID.toLong()
        submission.memberID = member.idLong

        submissionDAO.create(submission)

        event.reply("Done").complete().deleteOriginal().queue()
    }
}