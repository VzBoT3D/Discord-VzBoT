package org.vzbot.discordbot.ezvz.commands.orderbuttons

import com.zellerfeld.zellerbotapi.annotations.DCButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordModal
import com.zellerfeld.zellerbotapi.discord.components.PermanentDiscordButton
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.OrderKitDAO
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.util.concurrent.TimeUnit

@DCButton
class CancelOrderButton(discordButton: DiscordButton = DiscordButton()) : PermanentDiscordButton("btn_ezvz_cancel_order", discordButton) {
    override fun execute(actionSender: ActionSender, hook: Message) {
        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (order.buyer != actionSender.getMember().idLong) {
            actionSender.respondText("You need to be the owner of the order to cancel the order.", true)
            return
        }

        val textInput = TextInput.create("orderNr", "OrderNumber: ${order.orderID}", TextInputStyle.SHORT).setPlaceholder("Write the orderID of your order in here!").build()

        val confirmModal = DiscordModal("Confirmation", execute = { sender, _, values -> run {

            val providedOrderID = values["orderNr"]!!.asString.toLongOrNull()

            if (providedOrderID == null) {
                sender.respondText("The provided id is incorrectly", true)
                return@DiscordModal
            }

            if (providedOrderID != order.orderID.toLong()) {
                sender.respondText("The provided id is incorrectly", true)
                return@DiscordModal
            }

            val orderInfoEmbed = hook.channel.retrieveMessageById(order.infoEmbedID).complete()

            val infoEmbed = EmbedBuilder(orderInfoEmbed.embeds[0])
            infoEmbed.modifyField("Status", "Canceled", false)

            orderInfoEmbed.editMessageComponents().queue()
            orderInfoEmbed.editMessageEmbeds(infoEmbed.build()).queue()

            if (order.supplier != 0L) {
                val member = VzBot.discord.getMemberById(order.supplier) ?: return@DiscordModal

                val orderNotificationChannel = VzBot.discord.getTextChannelById(VzBot.configFileManager.getOrderNotificationChannel()) ?: return@DiscordModal run { sender.respondText("Error while sending notification :(") }
                orderNotificationChannel.sendMessageEmbeds(defaultEmbed("${member.asMention} the Order ${order.orderID} has been cancelled.", Color.RED, "Order Canceled")).queue()
            }

            OrderDAO.remove(order)
            OrderKitDAO.deleteForOrder(order.orderID)
            
            sender.respondText("You have cancelled your order, this channel will be deleted in 10 seconds", true)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
        }}, mutableListOf(ActionRow.of(textInput)))

        actionSender.respondModal(confirmModal)
    }
}