package org.vzbot.discordbot.events

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.LocationGetter
import org.vzbot.discordbot.filemanagers.implementations.FlowChartFileManager
import org.vzbot.discordbot.models.*
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.io.File
import java.util.concurrent.TimeUnit

class STLConfigEvents: ListenerAdapter() {


    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        val buttonID = event.componentId
        val clicker = event.member ?: return

        if (event.channel !is TextChannel) return

        if (!buttonID.startsWith("c")) return
        if (!STLFinderManager.isConfiguring(clicker, event.message)) return event.replyEmbeds(defaultEmbed("Error", Color.RED, "This is not your stl configurator")).queue()

        println(buttonID)

        if (buttonID == "c_cancel") {
            STLFinderManager.resetMember(clicker)
            return event.message.delete().queue()
        }

        if (buttonID == "c_cancel_chart") {
            STLFinderManager.resetChart(clicker)
            Menu.chartMenu(event.message)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_new_chart") {
            return event.replyModal(Menu.newChartModal()).queue()
        }

        if (buttonID == "c_edit_chart") {
            if (VzBot.flowChartFileManager.getFlowCharts().isEmpty()) {
                return event.reply("There are no flowcharts yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            Menu.editChartMenu(event.message)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_cancel_edit_chart") {
            Menu.chartMenu(STLFinderManager.getMessageConfiguring(event.member!!))
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_view_charts") {
            if (VzBot.flowChartFileManager.getFlowCharts().isEmpty()) {
                return event.reply("There are no flowcharts yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            Menu.viewChartsMenu(event.message)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_delete_chart") {

            if (VzBot.flowChartFileManager.getFlowCharts().isEmpty()) {
                return event.reply("There are no flowcharts yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            Menu.deleteChartsMenu(event.message)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_chart_done") {
            val chart = STLFinderManager.getChartFromMember(event.member!!)
            chart.asYML(VzBot.flowChartFileManager.getYaml())

            VzBot.flowChartFileManager.saveFile()

            Menu.chartMenu(STLFinderManager.getMessageConfiguring(event.member!!))
            STLFinderManager.resetChart(event.member!!)

            return event.reply("Your chart has been saved.").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
        }

        if (buttonID == "c_create_point") {
            return event.replyModal(Menu.newPointModal()).queue()
        }

        if (buttonID == "c_change_name") {
            return event.replyModal(Menu.changePointNameModal()).queue()
        }

        if (buttonID == "c_edit_meta") {
            val msg = STLFinderManager.getMessageFromID(event.messageId)
            Menu.metaConfiguratorMenu(msg)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_create_meta") {
            return event.replyModal(Menu.createMetaModal()).queue()
        }

        if (buttonID == "c_edit_meta_dir") {
            val point = STLFinderManager.getCurrentPoint(clicker)

            if (point.value.isEmpty()) {
                return event.reply("This point does not have any metavalues yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            val msg = event.message
            Menu.editMetaSelectionMenu(point, msg)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_cancel_meta_edit") {
            Menu.metaConfiguratorMenu(event.message)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_upload_meta") {
            Menu.metaFileUploadMenu(event.message)

            STLFinderManager.addUploading(event.member!!, event.channel as TextChannel)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_meta_add_chart") {
            val chart = STLFinderManager.getChartFromMember(clicker)
            Menu.linkChartMenu(event.message, chart)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_delete_meta") {
            val point = STLFinderManager.getCurrentPoint(clicker)

            if (point.value.isEmpty()) {
                return event.reply("This point does not have any metavalues yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            Menu.deleteMetaSelectionMenu(point, event.message)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_meta_done") {
            val msg = STLFinderManager.getMessageFromID(event.messageId)
            Menu.metaConfiguratorMenu(msg)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_cancel_meta") {

            val currentPoint = STLFinderManager.getCurrentPoint(event.member!!)
            val msg = STLFinderManager.getMessageConfiguring(event.member!!)
            val chart = STLFinderManager.getChartFromMember(event.member!!)

            Menu.pointMenu(currentPoint, msg, chart)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        event.reply("There was an error while processing your request. Please report this to devin.").queue()
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {

        val modalID = event.modalId

        if (event.member == null) return

        if (modalID.startsWith("c_create_chart")) {
            val title = event.getValue("title")!!.asString

            if (VzBot.flowChartFileManager.hasFlowChart(title)) return event.reply("There is already a chart with this name").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }

            val startingPoint = Datapoint(title, mutableListOf())
            val chart = Flowchart(startingPoint)

            STLFinderManager.addChartToMember(event.member!!, chart)

            val msg = STLFinderManager.getMessageConfiguring(event.member!!)

            Menu.pointMenu(startingPoint, msg, chart)
            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_create_point")) {
            val title = event.getValue("title")!!.asString

            val point = Datapoint(title, mutableListOf())
            val chart = STLFinderManager.getChartFromMember(event.member!!)

            if (chart.hasPoint(point.title)) return event.reply("There is already a point with the given name!").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}

            STLFinderManager.getCurrentPoint(event.member!!).nextPoints += point
            STLFinderManager.setCurrentPoint(event.member!!, point)

            val msg = STLFinderManager.getMessageConfiguring(event.member!!)

            Menu.pointMenu(point, msg, chart)

            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_change_point")) {
            val title = event.getValue("title")!!.asString
            val chart = STLFinderManager.getChartFromMember(event.member!!)

            if (chart.hasPoint(title)) return event.reply("There is already a point with the given name!").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}

            val point = STLFinderManager.getCurrentPoint(event.member!!)
            point.title = title

            val msg = STLFinderManager.getMessageConfiguring(event.member!!)

            Menu.pointMenu(point, msg, chart)

            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_create_meta")) {
            val title = event.getValue("title")!!.asString
            val url = event.getValue("url")!!.asString

            val point = STLFinderManager.getCurrentPoint(event.member!!)

            if (point.value.any {it.getTitle() == title}) {
                return event.reply("There is already a meta with this title existing").queue()
            }

            point.value += StringMedia(title.replace(".", ""), url)

            val msg = STLFinderManager.getMessageConfiguring(event.member!!)
            Menu.metaConfiguratorMenu(msg)

            event.reply("").queue { it.deleteOriginal().queue() }
        }
    }

    override fun onSelectMenuInteraction(event: SelectMenuInteractionEvent) {
        val menuID = event.componentId
        val member = event.member!!

        if (!STLFinderManager.isConfiguring(member, event.message)) return event.replyEmbeds(defaultEmbed("Error", Color.RED, "This is not your stl configurator")).queue()

        if (menuID == "select_point") {
            val selectedPoint = event.selectedOptions[0].value
            val chart = STLFinderManager.getChartFromMember(member)

            if (!chart.hasPoint(selectedPoint)) {
                return event.reply("An error occurred while trying to navigate to your given point").queue()
            }

            val point = chart.getPoint(selectedPoint)
            STLFinderManager.setCurrentPoint(event.member!!, point)

            val msg = STLFinderManager.getMessageFromID(event.messageId)

            Menu.pointMenu(point, msg, chart)
            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (menuID == "select_meta") {
            val selectedMeta = event.selectedOptions[0].value

            val chart = STLFinderManager.getChartFromMember(member)

            if (!chart.getAllPoints().any { it.value.any { value -> value.getTitle() == selectedMeta } }) {
                return event.reply("An error occurred while trying to navigate to your given meta").queue()
            }

            val point = STLFinderManager.getCurrentPoint(member)
            point.value.removeIf { it.getTitle() ==  selectedMeta}

            val modal = Menu.createMetaModal()
            return event.replyModal(modal).queue()
        }

        if (menuID == "delete_meta") {
            val selectedMeta = event.selectedOptions[0].value

            val chart = STLFinderManager.getChartFromMember(member)

            if (!chart.getAllPoints().any { it.value.any { value -> value.getTitle() == selectedMeta } }) {
                return event.reply("An error occurred while trying to navigate to your given meta").queue()
            }

            val point = STLFinderManager.getCurrentPoint(member)

            val meta = point.value.first { it.getTitle() ==  selectedMeta }

            if (meta is STLMedia) {
                meta.getMeta().delete()
            }
            point.value -= meta

            val msg = STLFinderManager.getMessageConfiguring(member)

            Menu.metaConfiguratorMenu(msg)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (menuID == "chart_edit_menu") {
            val selectedChart = event.selectedOptions[0].value

            if (!VzBot.flowChartFileManager.hasFlowChart(selectedChart)) {
                event.reply("There was an error while selecting your chart").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}
                return
            }


            val chart = VzBot.flowChartFileManager.getFlowCharts().first { it.startPoint.title == selectedChart }

            STLFinderManager.addChartToMember(member, chart)

            Menu.pointMenu(chart.startPoint, event.message, chart)
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (menuID == "chart_delete_menu") {
            val selectedChart = event.selectedOptions[0].value

            if (!VzBot.flowChartFileManager.hasFlowChart(selectedChart)) {
                event.reply("There was an error while deleting your chart").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}
                return
            }

            VzBot.flowChartFileManager.deleteChart(selectedChart)
            Menu.chartMenu(event.message)
            return event.reply("The chart has been deleted").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
        }

        if (menuID == "select_link_chart") {
            val selectedChart = event.selectedOptions[0].value

            if (!VzBot.flowChartFileManager.hasFlowChart(selectedChart)) {
                event.reply("There was an error while deleting your chart").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}
                return
            }

            val chart = VzBot.flowChartFileManager.getFlowchart(selectedChart)!!
            val point = STLFinderManager.getCurrentPoint(member)

            if (point.value.any {it.getTitle() == selectedChart}) {
                event.reply("This chart has already been added to this point").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}
                return
            }

            point.value += ChartMedia(chart)
            Menu.metaConfiguratorMenu(event.message)

            return event.reply("").queue { it.deleteOriginal().queue() }
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.member == null) return
        if (event.member!!.user.isBot) return

        val member = event.member!!
        val message = event.message

        if (!STLFinderManager.isUploading(member)) return
        if (STLFinderManager.getUploadChannel(member) != event.channel) return
        if (message.attachments.size == 0) return

        val chart = STLFinderManager.getChartFromMember(member)
        val point = STLFinderManager.getCurrentPoint(member)

        for (attachment in message.attachments) {
            val dir = File(LocationGetter().getLocation().absolutePath + "/VZBot/charts/data/${chart.startPoint.title}/${point.title}/${attachment.fileName}")
            dir.parentFile.mkdirs()
            attachment.proxy.downloadToFile(dir).whenComplete{ file, _ -> run {
                if (point.value.any {it.getTitle() == file.name}) {
                    return@run event.message.addReaction(Emoji.fromFormatted("❌")).queue()
                }

                point.value += STLMedia(file)
                event.message.addReaction(Emoji.fromFormatted("✅")).queue { event.message.delete().queueAfter(10, TimeUnit.SECONDS) }
            } }
        }
    }
}