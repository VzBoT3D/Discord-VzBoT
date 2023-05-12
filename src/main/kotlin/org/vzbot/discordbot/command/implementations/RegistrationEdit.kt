package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.models.Registration
import org.vzbot.discordbot.models.registrationDAO
import kotlin.math.roundToLong

private val cmdData = Commands.slash("registrationedit", "edit an ID")
    .addOption(OptionType.NUMBER, "id", "id of the serial", true)
    .addOption(OptionType.STRING, "description", "new description of the id", false)
    .addOption(OptionType.STRING, "media", "new media of the id", false)
    .addOption(OptionType.STRING, "country", "new country of the id", false)

class RegistrationEdit : Command("registrationedit", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {
        val idDouble =
            event.getOption("id")!!.asString.toDoubleOrNull() ?: return event.reply("Given ID is invalid").queue()
        val id = idDouble.roundToLong()
        if (!registrationDAO.hasID(id)) {
            return event.reply("ID not found").queue()
        }

        if (!(member.hasPermission(Permission.ADMINISTRATOR) || registrationDAO.get(id).memberID == member.idLong)) {
            return event.reply("You must own the ID to change it").queue()
        }

        val registration = registrationDAO.get(id)

        val newDescription =
            if (event.getOption("description") != null) event.getOption("description")!!.asString else registration.description
        val newMedia =
            if (event.getOption("media") != null) event.getOption("media")!!.asString else registration.mediaURL
        val newCountry =
            if (event.getOption("country") != null) event.getOption("country")!!.asString else registration.country

        val newRegistration = Registration().apply {
            this.id = id
            this.date = registration.date
            this.mediaURL = newMedia
            this.description = newDescription
            this.country = newCountry
            this.memberID = registration.memberID
        }

        registrationDAO.update(newRegistration)
        event.reply("Edit successfully").queue()
    }
}
