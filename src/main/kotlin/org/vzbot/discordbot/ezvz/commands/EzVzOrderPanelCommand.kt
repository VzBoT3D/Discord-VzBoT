package org.vzbot.discordbot.ezvz.commands

import com.zellerfeld.zellerbotapi.annotations.DCSelection
import com.zellerfeld.zellerbotapi.discord.components.*
import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.command.isModerator
import org.vzbot.discordbot.daos.ApplicationDAO
import org.vzbot.discordbot.daos.KitDAO
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.OrderKitDAO
import org.vzbot.discordbot.ezvz.commands.orderbuttons.AcceptOrderButton
import org.vzbot.discordbot.ezvz.commands.orderbuttons.CancelOrderButton
import org.vzbot.discordbot.models.Order
import org.vzbot.discordbot.models.OrderKit
import org.vzbot.discordbot.util.Continent
import org.vzbot.discordbot.util.Country
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

const val COUNTRIES_URL = "https://github.com/VzBoT3D/Discord-VzBoT/blob/main/countries.txt"
const val CONTINENTS_URL = "https://github.com/VzBoT3D/Discord-VzBoT/blob/main/continent.txt"

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

        val kitOptions = KitDAO.dao.toList().map { SelectOption.of(it.name, it.name) }.toMutableList()
        if (kitOptions.isEmpty()) {
            actionSender.respondText("There are no kits yet, please create atleast 1 kit to create this panel", userOnly = true)
            return
        }


        val selectMenu = OrderMenu(SimpleSelectionMenu("Kits", options = kitOptions, minOptions = 1))

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
        if (!VzBot.ezVzFileManager.getEzVzStatus()) {
            sender.respondText("The EzVz program is currently out of order!", true)
            return
        }

        if (VzBot.ezVzFileManager.isBanned(sender.getMember())) {
            sender.respondText("You are not eligible to order from the EzVz program", true)
            return
        }

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

        val selectionValues = values

        val modal = DiscordModal("EzVz-Ordering", { sender, message, values ->
            run {
                val countryInput = values["country"]!!.asString
                val continentInput = values["continent"]!!.asString
                val colors = values["colors"]!!.asString

                if (!Country.hasCountry(countryInput)) {
                    sender.respondText(
                        "The country you provided is not supported. A full list can be found here: $COUNTRIES_URL",
                        userOnly = true
                    )
                    return@run
                }

                if (!Continent.isContinent(continentInput)) {
                    sender.respondText(
                        "The continent you provided is not supported. A full list can be found here: $CONTINENTS_URL",
                        userOnly = true
                    )
                    return@run
                }

                val continent = Continent.getContinent(continentInput)
                val country = Country.getCountry(countryInput)

                val orderNr = OrderDAO.generateID()

                val orderCategoryID = VzBot.configFileManager.getOrdersCategory()
                val orderCategory = VzBot.discord.getCategoryById(orderCategoryID)
                    ?: return@DiscordModal run { sender.respondText("There was an error while creating your text channel") }
                val orderChannel = VzBot.discord.createTextChannel("order-$orderNr", orderCategory).complete()

                val everyoneRole = VzBot.discord.publicRole
                val permissionOverride = orderChannel.upsertPermissionOverride(everyoneRole)

                permissionOverride.deny(Permission.VIEW_CHANNEL)
                permissionOverride.deny(Permission.MESSAGE_SEND)
                permissionOverride.queue()

                val permissionOverrideUser = orderChannel.upsertPermissionOverride(sender.getMember())
                permissionOverrideUser.grant(Permission.VIEW_CHANNEL)
                permissionOverrideUser.grant(Permission.MESSAGE_SEND)
                permissionOverrideUser.queue()


                val continentRole = continent.getRole()
                orderChannel.upsertPermissionOverride(continentRole).grant(Permission.VIEW_CHANNEL).queue()

                val infoEmbed = EmbedBuilder(defaultEmbed("Information and control about order: $orderNr"))
                infoEmbed.addField("Member", sender.getMember().effectiveName, true)
                infoEmbed.addField("Country", country.countryName, true)
                infoEmbed.addField("Continent", continent.title, true)
                infoEmbed.addField("Kits to print", selectionValues.joinToString(), false)
                infoEmbed.addField("Colors", colors, false)
                infoEmbed.addField("Status", "Open", false)

                val acceptOrder = DiscordButton(
                    label = "Accept",
                    buttonStyle = ButtonStyle.SUCCESS,
                    emoji = Emoji.fromUnicode("U+1F91D")
                )
                val cancelOrder = DiscordButton(
                    label = "Cancel",
                    buttonStyle = ButtonStyle.DANGER,
                    emoji = Emoji.fromUnicode("U+1F5D1")
                )
                val infoEmbedID =
                    orderChannel.sendMessageEmbeds(infoEmbed.build()).addActionRow(AcceptOrderButton(acceptOrder), CancelOrderButton(cancelOrder)).complete().idLong

                orderChannel.pinMessageById(infoEmbedID).queue()

                val order = Order().apply {
                    this.orderID = orderNr
                    this.buyer = sender.getMember().idLong
                    this.country = country
                    this.continent = continent
                    this.wishedColors = colors
                    this.orderChannel = orderChannel.idLong
                    this.infoEmbedID = infoEmbedID
                }

                for (kit in selectionValues) {
                    val orderKit = OrderKit().apply {
                        this.id = OrderKitDAO.generateID()
                        this.kitName = kit
                        this.orderNr = order.orderID
                    }

                    OrderKitDAO.add(orderKit)
                }

                OrderDAO.add(order)

                sender.respondEmbed(
                    defaultEmbed(
                        "Your order has been successfully created. To view your order head over to this channel: ${orderChannel.asMention}",
                        Color.GREEN,
                        "Success"
                    ), userOnly = true
                )
            }
        }, mutableListOf(country, continent, colors))

        sender.respondModal(modal)
    }
}

fun EmbedBuilder.modifyField(name: String, value: String, inline: Boolean) {
    this.fields.removeIf { it.name == name }
    this.fields.add(Field(name, value, inline))
}