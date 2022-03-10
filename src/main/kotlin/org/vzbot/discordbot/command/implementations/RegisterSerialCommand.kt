package org.vzbot.discordbot.command.implementations

import org.vzbot.discordbot.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.vzbot.discordbot.warnsystem.Registration
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

private val commandData = CommandData("register", "Will register a new Serial ID for the given user")
    .addOption(OptionType.USER, "user", "user who belongs the serial id", true)
    .addOption(OptionType.STRING, "description", "Describe the build", true)
    .addOption(OptionType.STRING,"country", "Country where the machine is located", false)
    .addOption(OptionType.STRING, "media", "URL to an Video or Image of the machine", false)

class RegisterSerialCommand: Command("register", commandData, true) {

    override fun execute(member: Member, event: SlashCommandEvent) {
        val user = event.getOption("user")!!.asUser
        val description = event.getOption("description")!!.asString
        var country = "?"
        var media = ""


        if (event.getOption("country") != null) {
            country = event.getOption("country")!!.asString
        }

        if (event.getOption("media") != null) {
            media = event.getOption("media")!!.asString
        }

        val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(Date(System.currentTimeMillis()))

        val registration = Registration()
        registration.id = registration.getNextFreeID()
        registration.memberID = user.idLong
        registration.country = country
        registration.description = description
        registration.mediaURL = media
        registration.date = date

        registration.getDAO().create(registration)

        val returnEmbed = EmbedBuilder()
        returnEmbed.setTitle("Success")
        returnEmbed.setColor(Color.GREEN)

        returnEmbed.addField("User", user.asMention, true)
        returnEmbed.addField("ID", registration.id.toString(), true)
        returnEmbed.addField("Description", registration.description, true)
        if (media != "")
            returnEmbed.addField("Media-URL", registration.mediaURL, true)

        returnEmbed.addField("Country", registration.country, true)
        returnEmbed.addField("Date", registration.date, true)

        val serialHolder = VzBot.discord.retrieveMemberById(user.id).complete()

        serialHolder.modifyNickname("${serialHolder.effectiveName} VZ.${registration.id}").queue()
        VzBot.discord.addRoleToMember(serialHolder, VzBot.discord.getRoleById("891629031349420032")!!).queue()

        event.replyEmbeds(returnEmbed.build()).queue()
    }
}