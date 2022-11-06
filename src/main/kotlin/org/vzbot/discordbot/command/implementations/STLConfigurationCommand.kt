package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.vzbot.discordbot.command.Command

private val cmdData = Commands.slash("STLConfig", "Starts the process of the stl picker configuration")

class STLConfigurationCommand: Command("STLConfig", cmdData , true) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {




    }
}