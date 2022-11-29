package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.util.STLConfigurationManager
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

private val cmdData = Commands.slash("stlfinder", "Navigate trough our files to find your desired stl")

class STLFinderCommand: Command("stlfinder", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {

        val embed = defaultEmbed(
            "Welcome to the VzBoT **STL-Finder**.\n Use the blue buttons to navigate to a section. In every section there are multiple topics leading you to your stls. " +
                    "\nJust select your configuration from the given options.\n You can also search directly for stl files by using the search option.",
            Color.GREEN,
            "STL-Finder"
        )

        val charts = VzBot.flowChartFileManager.getFlowCharts()
        val chartList = mutableListOf<Button>()

        for (chart in charts) {
            chartList += Button.primary("s_${chart.startPoint.title}", chart.startPoint.title)
        }

        event.replyEmbeds(embed)
            .addActionRows(ActionRow.of(chartList), ActionRow.of(Button.secondary("s_search", "Search for an specific file")), ActionRow.of(Button.danger("s_cancel", "Cancel"))).queue { it ->
            it.retrieveOriginal().queue { STLFinderManager.addUserSTLFinding(member, it) }
        }
    }
}