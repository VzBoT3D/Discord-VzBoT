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
import org.vzbot.discordbot.models.Datapoint
import org.vzbot.discordbot.models.Flowchart
import org.vzbot.discordbot.models.STLMedia
import org.vzbot.discordbot.models.StringMedia
import org.vzbot.discordbot.util.STLFinderManager
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color
import java.io.File
import java.util.concurrent.TimeUnit

class STLConfigEvents: ListenerAdapter() {


    override fun onButtonInteraction(event: ButtonInteractionEvent) {

        val buttonID = event.componentId
        val clicker = event.member ?: return

        if (!buttonID.startsWith("c")) return
        if (!STLFinderManager.isConfiguring(clicker, event.message)) return event.replyEmbeds(defaultEmbed("Error", Color.RED, "This is not your stl configurator")).queue()

        println(buttonID)

        if (buttonID == "c_cancel") {
            STLFinderManager.resetMember(clicker)
            return event.message.delete().queue()
        }

        if (buttonID == "c_new_chart") {
            val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
            val modal = Modal.create("c_create_chart+${event.message.id}", "Create a new chart")
            modal.addActionRows(ActionRow.of(name))

            return event.replyModal(modal.build()).queue()
        }

        if (buttonID == "c_create_point") {
            val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
            val modal = Modal.create("c_create_point+${event.message.id}", "Create a new point")
            modal.addActionRows(ActionRow.of(name))

            return event.replyModal(modal.build()).queue()
        }

        if (buttonID == "c_change_name") {
            val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
            val modal = Modal.create("c_change_point+${event.message.id}", "Enter the new name of this point")
            modal.addActionRows(ActionRow.of(name))
            return event.replyModal(modal.build()).queue()
        }

        if (buttonID == "c_edit_meta") {
            val msg = STLFinderManager.getMessageFromID(event.messageId)

            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
                ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
            msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_create_meta") {
            val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
            val url = TextInput.create("url", "URL", TextInputStyle.PARAGRAPH).setPlaceholder("Link to where this meta is directing to").build()
            val modal = Modal.create("c_create_meta+${event.message.id}", "Create a new meta value")
            modal.addActionRows(ActionRow.of(name), ActionRow.of(url))
            return event.replyModal(modal.build()).queue()
        }

        if (buttonID == "c_edit_meta_dir") {
            val menu = SelectMenu.create("select_meta")
            val point = STLFinderManager.getCurrentPoint(clicker)

            if (point.value.isEmpty()) {
                return event.reply("This point does not have any metavalues yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            for (meta in point.value) {
                menu.addOption(meta.getTitle(), meta.getTitle())
            }

            val msg = event.message

            msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to edit")).queue()
            msg.editMessageComponents(ActionRow.of(menu.build()), ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel"))).queue()
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_cancel_meta_edit") {
            val msg = STLFinderManager.getMessageFromID(event.messageId)

            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
                ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
            msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_delete_meta") {
            val menu = SelectMenu.create("delete_meta")
            val point = STLFinderManager.getCurrentPoint(clicker)

            if (point.value.isEmpty()) {
                return event.reply("This point does not have any metavalues yet").queue { it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS) }
            }

            for (meta in point.value) {
                menu.addOption(meta.getTitle(), meta.getTitle())
            }

            val msg = event.message

            msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to delete.")).queue()
            msg.editMessageComponents(ActionRow.of(menu.build()), ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel"))).queue()
            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_meta_done") {
            val msg = STLFinderManager.getMessageFromID(event.messageId)

            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
                ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
            msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (buttonID == "c_cancel_meta") {
            val point = STLFinderManager.getCurrentPoint(event.member!!)
            val embed = configEmbed(point)
            val msg = STLFinderManager.getMessageFromID(event.messageId)
            val chart = STLFinderManager.getChartFromMember(event.member!!)

            val menu = SelectMenu.create("select_point")

            if (chart.getAllPoints().size > 1) {
                for (differentPoint in chart.getAllPoints()) {
                    menu.addOption(differentPoint.title, differentPoint.title)
                }
            }

            msg.editMessageEmbeds(embed).queue()

            if (chart.getAllPoints().size > 1) {
                msg.editMessageComponents(
                    ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                    ActionRow.of(menu.build())
                    ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                    ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()
            } else {
                msg.editMessageComponents(
                    ActionRow.of(Button.primary("c_create_point", "Create a new point"))
                    ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                    ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()
            }

            return event.reply("").queue { it.deleteOriginal().queue() }
        }

        event.reply("There was an error while processing your request. Please report this to devin.").queue()

    }

    override fun onModalInteraction(event: ModalInteractionEvent) {

        val modalID = event.modalId

        if (event.member == null) return

        if (modalID.startsWith("c_create_chart")) {
            val messageID = modalID.split("+")[1]
            val title = event.getValue("title")!!.asString

            val startingPoint = Datapoint(title, mutableListOf())

            val chart = Flowchart(startingPoint)

            STLFinderManager.addChartToMember(event.member!!, chart)

            val embed = configEmbed(startingPoint)
            val msg = STLFinderManager.getMessageFromID(messageID)

            msg.editMessageEmbeds(embed).queue()
            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_point", "Create a new point"))
                ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()

            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_create_point")) {
            val messageID = modalID.split("+")[1]
            val title = event.getValue("title")!!.asString

            val point = Datapoint(title, mutableListOf())
            val chart = STLFinderManager.getChartFromMember(event.member!!)

            if (chart.hasPoint(point.title)) return event.reply("There is already a point with the given name!").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}

            STLFinderManager.getCurrentPoint(event.member!!).nextPoints += point
            STLFinderManager.setCurrentPoint(event.member!!, point)

            val embed = configEmbed(point)
            val msg = STLFinderManager.getMessageFromID(messageID)

            val menu = SelectMenu.create("select_point")
            for (differentPoint in chart.getAllPoints()) {
                menu.addOption(differentPoint.title, differentPoint.title)
            }

            msg.editMessageEmbeds(embed).queue()
            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                ActionRow.of(menu.build())
                ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()

            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_change_point")) {
            val messageID = modalID.split("+")[1]
            val title = event.getValue("title")!!.asString

            val chart = STLFinderManager.getChartFromMember(event.member!!)

            if (chart.hasPoint(title)) return event.reply("There is already a point with the given name!").queue {it.deleteOriginal().queueAfter(10, TimeUnit.SECONDS)}

            val point = STLFinderManager.getCurrentPoint(event.member!!)
            point.title = title

            val embed = configEmbed(point)
            val msg = STLFinderManager.getMessageFromID(messageID)

            val menu = SelectMenu.create("select_point")

            if (chart.getAllPoints().size > 1) {
                for (differentPoint in chart.getAllPoints()) {
                    menu.addOption(differentPoint.title, differentPoint.title)
                }
            }

            msg.editMessageEmbeds(embed).queue()

            if (chart.getAllPoints().size > 1) {
                msg.editMessageComponents(
                    ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                    ActionRow.of(menu.build())
                    ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                    ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()
            } else {
                msg.editMessageComponents(
                    ActionRow.of(Button.primary("c_create_point", "Create a new point"))
                    ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                    ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()
            }

            event.reply("").queue { it.deleteOriginal().queue() }
        }

        if (modalID.startsWith("c_create_meta")) {
            val messageID = modalID.split("+")[1]
            val title = event.getValue("title")!!.asString
            val url = event.getValue("url")!!.asString

            val point = STLFinderManager.getCurrentPoint(event.member!!)

            if (point.value.any {it.getTitle() == title}) {
                return event.reply("There is already a meta with this title existing").queue()
            }

            point.value += StringMedia(title, url)

            val msg = STLFinderManager.getMessageFromID(messageID)
            STLFinderManager.addUploading(event.member!!, msg.channel as TextChannel)

            msg.editMessageEmbeds(defaultEmbed("If you want to attach media to this meta, just upload it into this channel. Otherwise press the done button.")).queue()
            msg.editMessageComponents(ActionRow.of(Button.primary("c_meta_done", "Done"), Button.danger("c_cancel", "Cancel"))).queue()
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

            val embed = configEmbed(point)
            val msg = STLFinderManager.getMessageFromID(event.messageId)

            val menu = SelectMenu.create("select_point")
            for (differentPoint in chart.getAllPoints()) {
                menu.addOption(differentPoint.title, differentPoint.title)
            }

            msg.editMessageEmbeds(embed).queue()
            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_point", "Create a new point")),
                ActionRow.of(menu.build())
                ,ActionRow.of(Button.primary("c_edit_meta", "Edit the Meta values"), Button.primary("c_change_name", "Change point name"))
                ,ActionRow.of(Button.danger("c_cancel", "Cancel"))).queue()
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

            val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
            val url = TextInput.create("url", "URL", TextInputStyle.PARAGRAPH).setPlaceholder("Link to where this meta is directing to").build()
            val modal = Modal.create("c_create_meta+${event.message.id}", "Create a new meta value")
            modal.addActionRows(ActionRow.of(name), ActionRow.of(url))
            return event.replyModal(modal.build()).queue()
        }

        if (menuID == "delete_meta") {
            val selectedMeta = event.selectedOptions[0].value

            val chart = STLFinderManager.getChartFromMember(member)

            if (!chart.getAllPoints().any { it.value.any { value -> value.getTitle() == selectedMeta } }) {
                return event.reply("An error occurred while trying to navigate to your given meta").queue()
            }

            val point = STLFinderManager.getCurrentPoint(member)
            point.value.removeIf { it.getTitle() ==  selectedMeta}

            val msg = STLFinderManager.getMessageConfiguring(member)

            msg.editMessageComponents(
                ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
                ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
            msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()

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

        val point = STLFinderManager.getCurrentPoint(member)

        for (attachment in message.attachments) {
            attachment.proxy.downloadToFile(File(LocationGetter().getLocation().absolutePath + "/${attachment.fileName}")).whenComplete{ file, _ -> run {
                if (point.value.any {it.getTitle() == file.name}) {
                    return@run event.message.addReaction(Emoji.fromFormatted("❌")).queue()
                }

                point.value += STLMedia(file)
                event.message.addReaction(Emoji.fromFormatted("✅")).queue()
            } }
        }
    }

    private fun configEmbed(currentPoint: Datapoint): MessageEmbed {

        val embed = EmbedBuilder()
        embed.setTitle("Configurator")

        embed.addField("Current Point", currentPoint.title, false)
        embed.addField("Meta", currentPoint.value.joinToString { it.getTitle() }, false)

        embed.setColor(Color.GREEN)

        return embed.build()
    }
}