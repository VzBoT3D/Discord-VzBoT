package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.vzbot.discordbot.command.ContextCommand
import org.vzbot.discordbot.models.registrationDAO
import java.awt.Color

private val cmd = Commands.context(Command.Type.USER, "View Printer")

class SerialContextCommand: ContextCommand("View Printer", cmd) {

    override fun execute(executor: Member, target: Member, command: String, event: UserContextInteractionEvent) {
        val serials = registrationDAO.listAll().filter { it.memberID == target.idLong }

        if (serials.isEmpty()) {
            event.reply("No Serials found.").queue()
            return
        }

        val embeds = mutableListOf<MessageEmbed>()

        for (serial in serials) {
            val embed = EmbedBuilder()
            embed.setTitle("VZ Build ${serial.id}")
            embed.setColor(Color.GREEN)

            embed.addField("Builder", target.nickname ?: target.effectiveName, true)
            embed.addField("Description", serial.description, true)
            embed.addField("Media", serial.mediaURL,true)
            embeds.add(embed.build())
        }

        event.replyEmbeds(embeds).queue()
    }
}