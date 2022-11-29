package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.util.STLConfigurationManager
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color

private val cmdData = Commands.slash("stlconfig", "Starts the process of the stl picker configuration")

class STLConfigurationCommand: Command("stlconfig", cmdData , true) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {

        if (STLConfigurationManager.isConfiguring(member)) return event.replyEmbeds(defaultEmbed("You are already configuring the stl finder")).queue()

        val embed = defaultEmbed("Use the buttons below to access the STL Configuration Tool", Color.ORANGE,"STL-Finder Configurator")

        event.replyEmbeds(embed).addActionRow(Button.primary("c_new_chart", "Create new chart"), Button.primary("c_view_charts", "View existing charts"))
            .addActionRow(Button.primary("c_edit_chart", "Edit an existing chart"), Button.primary("c_delete_chart", "Delete a existing chart"))
            .addActionRow(Button.danger("c_cancel", "Cancel")).queue { it ->
                it.retrieveOriginal().queue { STLConfigurationManager.addConfiguring(member, it) }
            }
    }
}