package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.vzbot.discordbot.command.Command
import org.vzbot.discordbot.models.STLFinderMenu
import org.vzbot.discordbot.util.STLConfigurationManager
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.util.concurrent.TimeUnit

private val cmdData = Commands.slash("stlfinder", "Navigate trough our files to find your desired stl")

class STLFinderCommand: Command("stlfinder", cmdData, false) {

    override fun execute(member: Member, event: SlashCommandInteractionEvent) {

        STLFinderManager.reset(member)

        if (STLFinderManager.hasUserSTLFinder(member)) {
             return event.replyEmbeds(defaultEmbed("You have already opened another STLFinder.")).queue()
         }

        val charts = VzBot.flowChartFileManager.getFlowCharts()
        val chartList = mutableListOf<Button>()

        for (chart in charts) {
            chartList += Button.primary("s_${chart.startPoint.title}", chart.startPoint.title)
        }

        if (chartList.isEmpty()) {
            return event.reply("There are currently no STLs yet.").queue()
        }

        event.replyEmbeds(defaultEmbed("Loading STLFinder...", Color.ORANGE, "STLFinder")).queue { it.retrieveOriginal().queue {
            STLFinderManager.addUserSTLFinding(member, it)
            STLFinderMenu.chartMenu(it)
        } }
    }
}