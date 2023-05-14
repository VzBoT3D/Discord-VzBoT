package org.vzbot.discordbot.ezvz.commands.orderbuttons

import com.zellerfeld.zellerbotapi.annotations.DCButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordModal
import com.zellerfeld.zellerbotapi.discord.components.PermanentDiscordButton
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.StatsDAO
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color

@DCButton
class RevokeOrderButton(discordButton: DiscordButton = DiscordButton()) : PermanentDiscordButton("btn_ezvz_revoke_order", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {

        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (actionSender.getMember().idLong != order.supplier) {
            actionSender.respondText("You are not the supplier of this order and can therefore not revoke it", true)
            return
        }


        val textInput = TextInput.create("orderNr", "OrderNumber: ${order.orderID}", TextInputStyle.SHORT).setPlaceholder("Write the orderID of the order in here!").build()

        val confirmModal = DiscordModal("Confirmation", execute = { sender, _, values -> run {

            val providedOrderID = values["orderNr"]!!.asString.toLongOrNull()

            if (providedOrderID == null) {
                actionSender.respondText("The provided id is incorrectly", true)
                return@DiscordModal
            }

            if (providedOrderID != order.orderID.toLong()) {
                actionSender.respondText("The provided id is incorrectly", true)
                return@DiscordModal
            }

            val stats = StatsDAO.get(order.supplier)
            stats.canceled++
            StatsDAO.dao.update(stats)

            order.supplier = 0
            order.supplierAgreed = false
            order.buyerAgreed = false
            order.orderState = OrderState.OPEN
            OrderDAO.dao.update(order)


            val orderInfoEmbed = hook.channel.retrieveMessageById(order.infoEmbedID).complete()

            val infoEmbed = EmbedBuilder(orderInfoEmbed.embeds[0])
            infoEmbed.modifyField("Status", "Open", false)

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

            orderInfoEmbed.editMessageComponents(ActionRow.of(AcceptOrderButton(acceptOrder), CancelOrderButton(cancelOrder))).queue()
            orderInfoEmbed.editMessageEmbeds(infoEmbed.build()).queue()

            hook.channel.sendMessageEmbeds(defaultEmbed("The supplier has revoked this order and therefore will not supply any printed parts. This order was reset back to the queue. This will be marked in the history of the supplier.", Color.RED, "CANCELED")).queue()

            sender.respondText("You have revoked this order. This will be noted in your data.", true)
        }}, mutableListOf(ActionRow.of(textInput)))

        actionSender.respondModal(confirmModal)
    }
}
