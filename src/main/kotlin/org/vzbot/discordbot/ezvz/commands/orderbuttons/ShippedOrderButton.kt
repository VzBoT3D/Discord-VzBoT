package org.vzbot.discordbot.ezvz.commands.orderbuttons

import com.zellerfeld.zellerbotapi.discord.components.DiscordButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordModal
import com.zellerfeld.zellerbotapi.discord.components.PermanentDiscordButton
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
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

class ShippedOrderButton(discordButton: DiscordButton = DiscordButton()): PermanentDiscordButton("btn_ezvz_order_shipped", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {

        val textChannel = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(textChannel)) {
            actionSender.respondText("There was an error while accepting this offer. Unknown Order Channel ID", true)
            return
        }

        val order = OrderDAO.getOrder(textChannel)

        if (order.supplier != actionSender.getMember().idLong) {
            actionSender.respondText("Only the supplier can ship the order!", true)
            return
        }

        val urlTextInput = TextInput.create("url", "Tracking-URL", TextInputStyle.SHORT).setMinLength(3).build()

        val modal = DiscordModal("Shipping URL", {sender,_,values -> run {

            val trackingInput = values["url"]!!.asString

            val shippedEmbed = EmbedBuilder(defaultEmbed("The order has been shipped by the supplier! You can track the package, and as soon as it arrives hit the finish button.", Color.GREEN, "SHIPPED!"))
            shippedEmbed.addField("TrackingURL", trackingInput, false)

            val orderInfoEmbedMessage = hook.channel.retrieveMessageById(order.infoEmbedID).complete()
            val orderInfoEmbed = EmbedBuilder(orderInfoEmbedMessage.embeds[0])
            orderInfoEmbed.modifyField("Status", "Shipped", false)

            orderInfoEmbedMessage.editMessageEmbeds(orderInfoEmbed.build()).queue()

            val finishOrderButton = DiscordButton(label = "Finish", buttonStyle = ButtonStyle.SUCCESS, emoji = Emoji.fromUnicode("U+263A"))
            val revokeButton = DiscordButton(label = "Revoke", buttonStyle = ButtonStyle.DANGER, emoji = Emoji.fromUnicode("U+1F614"))

            orderInfoEmbedMessage.editMessageComponents(
                ActionRow.of(FinishOrderButton(finishOrderButton)),
                ActionRow.of(RevokeOrderButton(revokeButton))).queue()

            hook.channel.sendMessageEmbeds(shippedEmbed.build()).queue {
                hook.channel.pinMessageById(it.id).queue()
            }

            order.orderState = OrderState.SHIPPED
            OrderDAO.dao.update(order)

            sender.respondText("You have successfully shipped the package!", true)
        }}, mutableListOf(ActionRow.of(urlTextInput)))

        actionSender.respondModal(modal)
    }

}