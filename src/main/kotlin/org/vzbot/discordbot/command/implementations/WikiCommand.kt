package org.vzbot.discordbot.command.implementations

import org.vzbot.discordbot.command.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData

private val cmdData = CommandData("wiki", "Browse trough the wiki with different keywords")
    .addOption(OptionType.STRING, "keywords", "Your search request. The bot will search the wiki for the given words", true)

class WikiCommand: Command("wiki", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandEvent) {
    }

}