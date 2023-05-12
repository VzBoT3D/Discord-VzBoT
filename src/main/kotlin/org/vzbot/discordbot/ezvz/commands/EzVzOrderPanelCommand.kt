package org.vzbot.discordbot.ezvz.commands

import com.zellerfeld.zellerbotapi.ZellerBot
import com.zellerfeld.zellerbotapi.annotations.DCSelection
import com.zellerfeld.zellerbotapi.discord.components.DiscordModal
import com.zellerfeld.zellerbotapi.discord.components.PermanentSimpleSelectionMenu
import com.zellerfeld.zellerbotapi.discord.components.SimpleSelectionMenu
import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.command.isModerator
import org.vzbot.discordbot.daos.KitDAO
import org.vzbot.discordbot.models.Order
import org.vzbot.discordbot.util.Country
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DCommand("sendorderpanel", "Creates a EzVz Ordering Panel in this channel")
class EzVzOrderPanelCommand : DiscordCommand() {

    override fun execute(actionSender: ActionSender) {
        if (!actionSender.getMember().isModerator()) {
            actionSender.respondText("You're missing the permission for this action!", userOnly = true)
            return
        }

        val kitOptions = KitDAO.dao.toList().map { SelectOption.of(it.name, it.description) }.toMutableList()
        if (kitOptions.isEmpty()) {
            actionSender.respondText("There are no kits yet, please create atleast 1 kit to create this panel", userOnly = true)
            return
        }


        val selectMenu = OrderMenu(SimpleSelectionMenu("Kits", options = kitOptions))

        val embed = EmbedBuilder(
            defaultEmbed(
                "Click on the menu below to select which kit of printed parts you would like to order",
                Color.RED,
                "EzVz-Ordering",
            ),
        )

        for (kit in KitDAO.dao.toList()) {
            embed.addField(kit.name, kit.description, false)
        }

        actionSender.respondEmbed(embed.build(), userOnly = false, ActionRow.of(selectMenu))
    }
}

@DCSelection
class OrderMenu(menu: SimpleSelectionMenu = SimpleSelectionMenu()) :
    PermanentSimpleSelectionMenu("ezvz_menu_order", menu) {

    override fun execute(values: List<String>, sender: ActionSender, hook: Message) {
        val country =
            ActionRow.of(
                TextInput.create("country", "Country", TextInputStyle.SHORT)
                    .setPlaceholder("From which country are you?").build(),
            )
        val continent = ActionRow.of(
            TextInput.create("continent", "Continent", TextInputStyle.SHORT)
                .setPlaceholder("From which continent are you").build(),
        )
        val colors = ActionRow.of(
            TextInput.create("colors", "Colors", TextInputStyle.SHORT)
                .setPlaceholder("Accent: Red, Main: Black").build(),
        )

        val modal = DiscordModal("EzVz-Ordering", { sender, message, values ->
            run {
                val country = values["country"]!!.asString
                val continent = values["continent"]!!.asString
                val colors = values["colors"]!!.asString

                if (Country.values().map { it.countryName }.none { it == country }) {
                    sender.respondText("The country you provided is not supported", userOnly = true)
                    return@run
                }

                val orderCategory = sender.getMember().

                val order = Order().apply {
                    this.buyer = sender.getMember().idLong
                    this.country = country
                    this.continent = continent
                    this.wishedColors = colors
                }

                sender.respondText("Country: $country, Continent: $continent, Colors: $colors")
            }
        }, mutableListOf(continent, country, colors))

        sender.respondModal(modal)
    }
}
