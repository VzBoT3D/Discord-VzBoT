package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import org.vzbot.discordbot.warnsystem.registrationDAO
import java.awt.Color
import kotlin.math.roundToLong

private val cmdData = Commands.slash("viewbuild", "view an official vzbuild").addOption(OptionType.NUMBER, "id", "Serial id of the build", true)

class ViewBuildCommand: Command("viewbuild", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {
        val id = event.getOption("id")!!.asString.toDoubleOrNull()

        if (id == null) {
            event.replyEmbeds(defaultEmbed("Given id is no long")).queue()
            return
        }


        val idLong = id.roundToLong()

        if (!registrationDAO.hasID(idLong)) {
            event.replyEmbeds(defaultEmbed("This id is not registered yet.")).queue()
            return
        }

        val reg = registrationDAO.get(idLong)

        val embed = EmbedBuilder()
        embed.setTitle("VZ Build $idLong")
        embed.setColor(Color.GREEN)

        val builder = VzBot.discord.retrieveMemberById(reg.memberID).complete()

        embed.addField("Builder", builder.nickname ?: builder.effectiveName, true)
        embed.addField("Description", reg.description, true)
        embed.addField("Media", reg.mediaURL,true)

        event.replyEmbeds(embed.build()).queue()

    }

}