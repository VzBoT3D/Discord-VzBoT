package org.vzbot.discordbot.models

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

object STLFinderMenu {

    fun stlSearchQueryModal(): Modal {
        val modal = Modal.create("s_search_stl", "Search for a stl file")
        modal.addActionRow(TextInput.create("stl_search_query", "Query", TextInputStyle.SHORT).build())
        return modal.build()
    }

    fun chartMenu(msg: Message) {
        val charts = VzBot.flowChartFileManager.getFlowCharts()
        val chartList = mutableListOf<Button>()

        for (chart in charts) {
            chartList += Button.primary("s_${chart.startPoint.title}", chart.startPoint.title)
        }

        msg.editMessageComponents(ActionRow.of(chartList), ActionRow.of(Button.secondary("s_search", "Search for a specific file")), ActionRow.of(Button.danger("s_cancel", "Cancel"))).queue()
        val embed = defaultEmbed(
            "Welcome to the VzBoT **STL-Finder**.\n Use the blue buttons to navigate to a section. In every section there are multiple topics leading you to your stls. " +
                    "\nJust select your configuration from the given options.\n You can also search directly for stl files by using the search option.",
            Color.GREEN,
            "STL-Finder"
        )
        msg.editMessageEmbeds(embed).queue()
    }

    fun currentPointMenu(msg: Message, chart: Flowchart, currentPoint: Datapoint, previousPoints: List<Datapoint>) {

        val embed = EmbedBuilder(defaultEmbed("Use the blue buttons below to dive deeper into the configuration.", Color.GREEN, chart.startPoint.title, "VzBot STL-Finder"))

        embed.addField("Current Configuration", previousPoints.joinToString(separator = " ") { "-> ${it.title}" } + " -> ${currentPoint.title}", false)
        embed.addField("Current Topic", currentPoint.title, false)

        if (currentPoint.value.any { it is StringMedia} ) {
            embed.addField("Information", currentPoint.value.filterIsInstance<StringMedia>().joinToString(separator = "\n") { "-> ${it.getMeta()}" }, false)
        }

        if (currentPoint.value.any { it is STLMedia }) {
            embed.addField("Files", currentPoint.value.filterIsInstance<STLMedia>().joinToString(separator = "\n") { "-> ${it.getMetaRaw().split(
                    "[\\\\\\/]".toRegex()).last()}" }, false)
        }

        msg.editMessageEmbeds(embed.build()).queue()

        val chartList = mutableListOf<Button>()

        for (point in currentPoint.nextPoints) {
            chartList += Button.primary("s_${point.title}", point.title)
        }

        for (chart in currentPoint.value.filterIsInstance<ChartMedia>()) {
            chartList += Button.primary("s_${chart.getMeta().startPoint.title}", chart.getMeta().startPoint.title)
        }

        if (chartList.isNotEmpty()) {
            if (currentPoint.value.any { it is STLMedia }) {
                msg.editMessageComponents(ActionRow.of(chartList), ActionRow.of(Button.secondary("s_search", "Search for a specific file"), Button.secondary("s_download_files", "Download files")), ActionRow.of(Button.primary("s_back", "Back"), Button.danger("s_cancel", "Cancel"))).queue()
            } else {
                msg.editMessageComponents(ActionRow.of(chartList), ActionRow.of(Button.secondary("s_search", "Search for a specific file")), ActionRow.of(Button.primary("s_back", "Back"), Button.danger("s_cancel", "Cancel"))).queue()
            }
        } else {
            if (currentPoint.value.any { it is STLMedia }) {
                msg.editMessageComponents(ActionRow.of(Button.secondary("s_search", "Search for a specific file"), Button.secondary("s_download_files", "Download files")), ActionRow.of(Button.primary("s_back", "Back"), Button.danger("s_cancel", "Cancel"))).queue()
            } else {
                msg.editMessageComponents(ActionRow.of(Button.secondary("s_search", "Search for a specific file")), ActionRow.of(Button.primary("s_back", "Back"), Button.danger("s_cancel", "Cancel"))).queue()
            }
        }
    }

    fun foundFilesMenu(files: List<STLMedia>, msg: Message, query: String) {

        val embed = EmbedBuilder(defaultEmbed("The following files have been found", Color.ORANGE, "Filefinder"))

        embed.addField("Found files", files.joinToString(separator = "\n") {
            it.getMetaRaw().split("[\\\\\\/]".toRegex()).last().replace(query, "**$query**", ignoreCase = true)
        }, false)


        msg.editMessageEmbeds(embed.build()).queue()
        msg.editMessageComponents(ActionRow.of(Button.secondary("s_search", "Search for another file"), Button.secondary("s_download_files", "Download found files")), ActionRow.of(Button.danger("s_back_search", "Back"))).queue()
    }
}