package org.vzbot.discordbot.events

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import net.lingala.zip4j.ZipFile
import org.vzbot.discordbot.LocationGetter
import org.vzbot.discordbot.models.STLFinderMenu
import org.vzbot.discordbot.models.STLMedia
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.vzbot.VzBot
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class STLFinderEvents : ListenerAdapter() {

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val buttonID = event.componentId
        val clicker = event.member ?: return

        if (!buttonID.startsWith("s")) return
        if (event.channel !is TextChannel) return

        if (!STLFinderManager.isUserSTLFinding(clicker, event.message)) {
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "s_cancel") {
            STLFinderManager.reset(clicker)
            event.message.delete().queue()
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "s_search") {
            return event.replyModal(STLFinderMenu.stlSearchQueryModal()).queue()
        }

        if (buttonID == "s_back") {
            if (STLFinderManager.getPreviousPoints(clicker).isEmpty()) {
                STLFinderManager.removeCurrentPoint(clicker)
                STLFinderMenu.chartMenu(event.message)
                return event.reply("").queue { it.deleteOriginal().queue() }
            }

            val previousPoint = STLFinderManager.getPreviousPoints(clicker).last()

            val chart = if (VzBot.flowChartFileManager.hasFlowChart(previousPoint.title)) {
                VzBot.flowChartFileManager.getFlowchart(previousPoint.title)!!
            } else {
                STLFinderManager.getUserChart(clicker)
            }

            STLFinderManager.addUserChart(clicker, chart)

            STLFinderManager.removePreviousPoint(clicker, previousPoint)
            STLFinderManager.setCurrentPoint(clicker, previousPoint)

            STLFinderMenu.currentPointMenu(
                event.message,
                chart,
                previousPoint,
                STLFinderManager.getPreviousPoints(clicker),
            )
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "s_back_search") {
            if (!STLFinderManager.hasCurrentPoint(clicker)) {
                STLFinderMenu.chartMenu(event.message)
                return event.reply("").queue { it.deleteOriginal().queue() }
            }

            val currentPoint = STLFinderManager.getCurrentPoint(clicker)
            val chart = STLFinderManager.getUserChart(clicker)

            STLFinderMenu.currentPointMenu(
                event.message,
                chart,
                currentPoint,
                STLFinderManager.getPreviousPoints(clicker),
            )
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "s_download_files") {
            val files = STLFinderManager.getFoundFiles(clicker).map { it.getMeta() }

            if (files.isEmpty()) {
                return event.reply("There are no files to download.")
                    .queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            event.deferReply().queue()

            val tc = event.channel as TextChannel
            val chart =
                if (STLFinderManager.hasUserChart(clicker)) STLFinderManager.getUserChart(clicker).startPoint.title else "VzBoT"

            val zip = ZipFile("${LocationGetter().getLocation()}/VZBoT/temp/$chart ${clicker.effectiveName}.zip")

            for (file in files) {
                zip.addFile(file)
            }

            val size = Files.size(zip.file.toPath()) / 1024 / 1024

            if (size > 99) {
                return event.hook.sendMessage("The selected files are over 100mb in size. Please select a more narrow selection")
                    .queue()
            }

            tc.sendFiles(FileUpload.fromData(zip.file)).queue {
                run {
                    zip.file.delete()
                    it.delete().queueAfter(3, TimeUnit.MINUTES)
                    event.hook.sendMessage("The files will be deleted in 3 minutes.")
                        .queue { it.delete().queueAfter(10, TimeUnit.SECONDS) }
                }
            }

            return
        }

        val pointID = buttonID.substring(2)

        if (VzBot.flowChartFileManager.hasFlowChart(pointID)) {
            if (STLFinderManager.hasCurrentPoint(clicker)) {
                STLFinderManager.addPreviousPoint(clicker, STLFinderManager.getCurrentPoint(clicker))
            }

            val chart = VzBot.flowChartFileManager.getFlowchart(pointID)!!
            STLFinderManager.addUserChart(clicker, chart)
            STLFinderManager.setCurrentPoint(clicker, chart.startPoint)

            STLFinderManager.setFoundFiles(clicker, chart.startPoint.value.filterIsInstance<STLMedia>())

            STLFinderMenu.currentPointMenu(
                event.message,
                chart,
                chart.startPoint,
                STLFinderManager.getPreviousPoints(clicker),
            )
            return event.reply("").queue { it.deleteOriginal().queue() }
        } else if (STLFinderManager.hasUserChart(clicker)) {
            val chart = STLFinderManager.getUserChart(clicker)

            if (chart.getAllPoints().none { it.title == pointID }) {
                return event.reply("").queue { it.deleteOriginal().queue() }
            }

            val point = chart.getAllPoints().first { it.title == pointID }

            val currentPoint = STLFinderManager.getCurrentPoint(clicker)

            STLFinderManager.addPreviousPoint(clicker, currentPoint)
            STLFinderManager.setCurrentPoint(clicker, point)

            STLFinderManager.setFoundFiles(clicker, point.value.filterIsInstance<STLMedia>())

            STLFinderMenu.currentPointMenu(event.message, chart, point, STLFinderManager.getPreviousPoints(clicker))
            return event.reply("").queue { it.deleteOriginal().queue() }
        }
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val modalID = event.modalId
        if (event.member == null) return
        val member = event.member!!

        if (modalID == "s_search_stl") {
            val query = event.getValue("stl_search_query")!!.asString

            return if (!STLFinderManager.hasCurrentPoint(member)) {
                val startPoints = VzBot.flowChartFileManager.getFlowCharts()

                val allPoints = startPoints.map { it.getAllPoints() }.flatten()

                val allValues = allPoints.map { it.value }.flatten()

                val files =
                    allValues.filterIsInstance<STLMedia>().filter { it.getMetaRaw().contains(query, ignoreCase = true) }

                STLFinderManager.setFoundFiles(member, files)

                STLFinderMenu.foundFilesMenu(files, STLFinderManager.getMessage(member), query)
                event.reply("").queue { it.deleteOriginal().queue() }
            } else {
                val point = STLFinderManager.getCurrentPoint(member)
                val allPoints = point.getAllFollowingPoints()

                val allValues = allPoints.map { it.value }.flatten()

                val files =
                    allValues.filterIsInstance<STLMedia>().filter { it.getMetaRaw().contains(query, ignoreCase = true) }

                STLFinderManager.setFoundFiles(member, files)

                STLFinderMenu.foundFilesMenu(files, STLFinderManager.getMessage(member), query)
                event.reply("").queue { it.deleteOriginal().queue() }
            }
        }
    }
}
