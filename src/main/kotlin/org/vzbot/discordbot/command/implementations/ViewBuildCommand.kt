package org.vzbot.discordbot.command.implementations

import org.vzbot.discordbot.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.vzbot.discordbot.warnsystem.registrationDAO
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

private val cmdData = CommandData("viewbuild", "view an official vzbuild").addOption(OptionType.NUMBER, "id", "Serial id of the build", true)

class ViewBuildCommand: Command("viewbuild", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandEvent) {
        val id = event.getOption("id")!!.asString.toLong()

        val reg = registrationDAO.get(id)

        val embed = EmbedBuilder()
        embed.setTitle("VZ Build $id")
        embed.setColor(Color.GREEN)

        val builder = VzBot.discord.retrieveMemberById(reg.memberID).complete()

        embed.addField("Builder", builder.nickname, true)
        embed.addField("Description", reg.description, true)
        embed.addField("Media", reg.mediaURL,true)

        event.replyEmbeds(embed.build()).queue()

    }

}