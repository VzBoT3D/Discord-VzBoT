package org.vzbot.discordbot.models

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import org.vzbot.discordbot.LocationGetter
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.io.File
import java.nio.file.Files

object Menu {

    fun newChartModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val modal = Modal.create("c_create_chart", "Create a new chart")
        modal.addActionRows(ActionRow.of(name))
        return modal.build()
    }

    fun editChartMenu(msg: Message) {
        msg.editMessageEmbeds(
            defaultEmbed(
                "Select from the menu below the chart you would like to edit",
                Color.GREEN,
                "Selection",
            ),
        ).queue()

        val menu = StringSelectMenu.create("chart_edit_menu")

        for (chart in VzBot.flowChartFileManager.getFlowCharts()) {
            menu.addOption(chart.startPoint.title, chart.startPoint.title)
        }

        msg.editMessageComponents(
            ActionRow.of(menu.build()),
            ActionRow.of(Button.danger("c_cancel_edit_chart", "Cancel")),
        ).queue()
    }

    fun viewChartsMenu(msg: Message) {
        val embed = defaultEmbed("These are the currently existing charts: ", Color.ORANGE, "Charts")
        val builder = EmbedBuilder(embed)

        for (chart in VzBot.flowChartFileManager.getFlowCharts()) {
            builder.addField(
                chart.startPoint.title,
                "Points: ${chart.getAllPoints().size} \n Metas: ${
                    chart.getAllPoints().sumOf { it.value.size }
                } \n Attached files: ${
                    chart.getAllPoints().map { it.value }.sumOf { it.count { value -> value is STLMedia } }
                }",
                true,
            )
        }

        msg.editMessageEmbeds(builder.build()).queue()
        msg.editMessageComponents(ActionRow.of(Button.danger("c_cancel_edit_chart", "Cancel"))).queue()
    }

    fun deleteChartsMenu(msg: Message) {
        msg.editMessageEmbeds(
            defaultEmbed(
                "Select from the menu below the chart you would like to delete",
                Color.GREEN,
                "Selection",
            ),
        ).queue()

        val menu = StringSelectMenu.create("chart_delete_menu")

        for (chart in VzBot.flowChartFileManager.getFlowCharts()) {
            menu.addOption(chart.startPoint.title, chart.startPoint.title)
        }

        msg.editMessageComponents(
            ActionRow.of(menu.build()),
            ActionRow.of(Button.danger("c_cancel_edit_chart", "Cancel")),
        ).queue()
    }

    fun newPointModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val modal = Modal.create("c_create_point", "Create a new point")
        modal.addActionRows(ActionRow.of(name))
        return modal.build()
    }

    fun changePointNameModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val modal = Modal.create("c_change_point", "Enter the new name of this point")
        modal.addActionRows(ActionRow.of(name))
        return modal.build()
    }

    fun metaConfiguratorMenu(msg: Message) {
        msg.editMessageComponents(
            ActionRow.of(
                Button.primary("c_create_meta", "Create new meta component"),
                Button.primary("c_meta_add_chart", "Link a different chart to this point"),
                Button.primary("c_upload_meta", "Upload files to this point"),
                Button.primary("c_edit_meta_dir", "Edit a certain meta"),
                Button.danger("c_delete_meta", "Delete meta component"),
            ),
            ActionRow.of(Button.danger("c_cancel_meta", "Back")),
        ).queue()
        msg.editMessageEmbeds(
            defaultEmbed(
                "Select from the options below how you want to change this point",
                Color.GREEN,
                "Meta editor",
            ),
        ).queue()
    }

    fun linkChartMenu(msg: Message, chart: Flowchart) {
        msg.editMessageEmbeds(
            defaultEmbed(
                "Select from the options which chart should be linked to this point",
                Color.GREEN,
                "Meta editor",
            ),
        ).queue()

        val menu = StringSelectMenu.create("select_link_chart")

        for (flowchart in VzBot.flowChartFileManager.getFlowCharts()
            .filter { it.startPoint.title != chart.startPoint.title }) {
            menu.addOption(flowchart.startPoint.title, flowchart.startPoint.title)
        }

        msg.editMessageComponents(
            ActionRow.of(menu.build()),
            ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel")),
        ).queue()
    }

    fun createMetaModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val url = TextInput.create("url", "URL", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Link to where this meta is directing to").build()
        val modal = Modal.create("c_create_meta", "Create a new meta value")

        modal.addActionRows(ActionRow.of(name), ActionRow.of(url))
        return modal.build()
    }

    fun editMetaModel(meta: SavedMedia<*>): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val url = TextInput.create("url", "URL", TextInputStyle.PARAGRAPH).setPlaceholder(
            if (meta.getMetaRaw().length > 99) meta.getMetaRaw().substring(0, 99) else meta.getMetaRaw(),
        ).build()
        val modal = Modal.create("c_edit_meta_modal+${meta.getTitle()}", "Edit a the ${meta.getTitle()} name and value")

        modal.addActionRows(ActionRow.of(name), ActionRow.of(url))
        return modal.build()
    }

    fun editMetaSelectionMenu(point: Datapoint, msg: Message) {
        val menu = StringSelectMenu.create("select_meta")

        for (meta in point.value.filterIsInstance<StringMedia>()) {
            menu.addOption(meta.getTitle(), meta.getTitle())
        }

        msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to edit")).queue()
        msg.editMessageComponents(
            ActionRow.of(menu.build()),
            ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel")),
        ).queue()
    }

    fun deleteMetaSelectionMenu(point: Datapoint, msg: Message) {
        val menu = StringSelectMenu.create("delete_meta")

        for (meta in point.value) {
            menu.addOption(meta.getTitle(), meta.getTitle())
        }

        msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to delete.")).queue()
        msg.editMessageComponents(
            ActionRow.of(menu.build()),
            ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel")),
        ).queue()
    }

    fun pointMenu(point: Datapoint, msg: Message, chart: Flowchart) {
        val embed = configPointEmbed(point)

        val menu = StringSelectMenu.create("select_point")

        if (chart.getAllPoints().size > 1) {
            for (differentPoint in chart.getAllPoints()) {
                menu.addOption(differentPoint.title, differentPoint.title)
            }
        }

        msg.editMessageEmbeds(embed).queue()

        if (chart.getAllPoints().size > 1) {
            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                ActionRow.of(menu.build()),
                ActionRow.of(
                    Button.primary("c_edit_meta", "Edit the Meta values"),
                    Button.primary("c_change_name", "Change point name"),
                    Button.danger("c_delete_point", "Delete point below"),
                ),
                ActionRow.of(Button.secondary("c_chart_done", "Done"), Button.danger("c_cancel_chart", "Cancel")),
            ).queue()
        } else {
            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                ActionRow.of(
                    Button.primary("c_edit_meta", "Edit the Meta values"),
                    Button.primary("c_change_name", "Change point name"),
                ),
                ActionRow.of(Button.secondary("c_chart_done", "Done"), Button.danger("c_cancel_chart", "Cancel")),
            ).queue()
        }
    }

    fun pointDeleteMenu(msg: Message, nextPoints: List<Datapoint>) {
        msg.editMessageEmbeds(defaultEmbed("Select the point you want to delete from the list below. This will also delete all points, the deleted point is pointing to."))
            .queue()

        val menu = StringSelectMenu.create("c_d_p_menu")

        for (point in nextPoints) {
            menu.addOption(point.title, point.title)
        }

        if (nextPoints.size == 1) {
            menu.addOption("Do not select!", "opt")
            menu.setDefaultValues(listOf("opt"))
        }

        menu.maxValues = 1

        msg.editMessageComponents(ActionRow.of(menu.build()), ActionRow.of(Button.danger("c_cancel_meta", "Cancel")))
            .queue()
    }

    fun chartMenu(msg: Message) {
        val embed = defaultEmbed(
            "Use the buttons below to access the STL Configuration Tool",
            Color.ORANGE,
            "STL-Finder Configurator",
        )

        val builder = EmbedBuilder(embed)

        val data = File(LocationGetter().getLocation().absolutePath + "/VZBoT/charts/data/")
        val fileSize = Files.size(data.toPath())

        val fileSizeMb = fileSize / 1024.0 / 1024.0

        val totalSpace = data.totalSpace
        val totalSpaceMb = totalSpace / 1024.0 / 1024.0

        val percent = fileSizeMb / totalSpaceMb * 100

        builder.addField("Storage uses", "$fileSizeMb/$totalSpaceMb **-**  $percent%", true)

        msg.editMessageEmbeds(builder.build()).queue()
        msg.editMessageComponents(
            ActionRow.of(
                Button.primary("c_new_chart", "Create new chart"),
                Button.primary("c_view_charts", "View existing charts"),
            ),
            ActionRow.of(
                Button.primary("c_edit_chart", "Edit an existing chart"),
                Button.primary("c_delete_chart", "Delete a existing chart"),
            ),
            ActionRow.of(Button.danger("c_cancel", "Cancel")),
        ).queue()
    }

    fun metaFileUploadMenu(msg: Message) {
        msg.editMessageEmbeds(defaultEmbed("Please upload your files to the channel. Press done when you are finished or cancel to cancel the process."))
            .queue()
        msg.editMessageComponents(
            ActionRow.of(
                Button.primary("c_meta_done", "Done"),
                Button.danger("c_cancel_meta", "Cancel"),
            ),
        ).queue()
    }

    private fun configPointEmbed(currentPoint: Datapoint): MessageEmbed {
        val embed = EmbedBuilder()
        embed.setTitle("Configurator")

        embed.addField("Current Point", currentPoint.title, false)
        embed.addField(
            "Meta",
            currentPoint.value.joinToString(separator = "") { run { if (it is StringMedia) "URL -> ${it.getTitle()}" else if (it is STLMedia) "File -> ${it.getTitle()}" else "Chart -> ${it.getTitle()}" } + "\n" },
            false,
        )

        embed.setColor(Color.GREEN)

        return embed.build()
    }
}
