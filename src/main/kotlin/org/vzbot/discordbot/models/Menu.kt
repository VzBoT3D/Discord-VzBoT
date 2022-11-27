package org.vzbot.discordbot.models

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color

object Menu {


    fun newChartModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val modal = Modal.create("c_create_chart", "Create a new chart")
        modal.addActionRows(ActionRow.of(name))
        return modal.build()
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

    fun editMetaMenu(msg: Message) {
        msg.editMessageComponents(
            ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
            ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
        msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()
    }

    fun createMetaModal(): Modal {
        val name = TextInput.create("title", "Name", TextInputStyle.SHORT).build()
        val url = TextInput.create("url", "URL", TextInputStyle.PARAGRAPH).setPlaceholder("Link to where this meta is directing to").build()
        val modal = Modal.create("c_create_meta", "Create a new meta value")
        modal.addActionRows(ActionRow.of(name), ActionRow.of(url))
        return modal.build()
    }

    fun createMetaMenu(msg: Message) {
        msg.editMessageComponents(
            ActionRow.of(Button.primary("c_create_meta", "Create new meta component"), Button.primary("c_edit_meta_dir", "Edit a certain meta"), Button.danger("c_delete_meta", "Delete meta component")),
            ActionRow.of(Button.danger("c_cancel_meta", "Cancel"))).queue()
        msg.editMessageEmbeds(defaultEmbed("Select from the options below how you want to change this point", Color.GREEN, "Meta editor")).queue()
    }

    fun editMetaSelectionMenu(point: Datapoint, msg: Message) {
        val menu = SelectMenu.create("select_meta")

        for (meta in point.value) {
            menu.addOption(meta.getTitle(), meta.getTitle())
        }

        msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to edit")).queue()
        msg.editMessageComponents(ActionRow.of(menu.build()), ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel"))).queue()
    }

    fun deleteMetaSelectionMenu(point: Datapoint, msg: Message) {
        val menu = SelectMenu.create("delete_meta")

        for (meta in point.value) {
            menu.addOption(meta.getTitle(), meta.getTitle())
        }

        msg.editMessageEmbeds(defaultEmbed("Select the Meta from the list below you would like to delete.")).queue()
        msg.editMessageComponents(ActionRow.of(menu.build()), ActionRow.of(Button.danger("c_cancel_meta_edit", "Cancel"))).queue()
    }

    private fun configPointEmbed(currentPoint: Datapoint): MessageEmbed {

        val embed = EmbedBuilder()
        embed.setTitle("Configurator")

        embed.addField("Current Point", currentPoint.title, false)
        embed.addField("Meta", currentPoint.value.joinToString { it.getTitle() }, false)

        embed.setColor(Color.GREEN)

        return embed.build()
    }

}