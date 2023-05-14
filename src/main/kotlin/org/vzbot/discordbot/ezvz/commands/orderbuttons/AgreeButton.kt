package org.vzbot.discordbot.ezvz.commands.orderbuttons

import com.zellerfeld.zellerbotapi.annotations.DCButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordButton
import com.zellerfeld.zellerbotapi.discord.components.PermanentDiscordButton
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.OrderKitDAO
import org.vzbot.discordbot.daos.StatsDAO
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color

@DCButton
class AgreeButton(discordButton: DiscordButton = DiscordButton()): PermanentDiscordButton("btn_ezvz_order_agree", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {
        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)


        if (actionSender.getMember().idLong != order.buyer && actionSender.getMember().idLong != order.supplier) {
            actionSender.respondText("Only the buyer and seller can agree this deal!", true)
            return
        }

        if (actionSender.getMember().idLong == order.buyer) {
            order.buyerAgreed = true
        } else {
            order.supplierAgreed= true
        }

        OrderDAO.dao.update(order)

        actionSender.respondText("You have agreed on the deal!", true)

        if (order.buyerAgreed && order.supplierAgreed) {
            order.orderState = OrderState.PRINTING
            OrderDAO.dao.update(order)

            val printingEmbed = EmbedBuilder(defaultEmbed("A deal was found. **The supplier can now start with printing the order!** These are the parts which are required to print:", Color.GREEN, "PRINTING"))

            printingEmbed.addField("Colors", order.wishedColors, false)

            for (kit in OrderKitDAO.getKitsForOrder(order.orderID)) {
                printingEmbed.addField(kit.name, kit.url, true)
            }

            hook.channel.sendMessageEmbeds(printingEmbed.build()).queue()

            val orderInfoEmbedMessage = hook.channel.retrieveMessageById(order.infoEmbedID).complete()
            val orderInfoEmbed = EmbedBuilder(orderInfoEmbedMessage.embeds[0])
            orderInfoEmbed.modifyField("Status", "Printing", false)

            orderInfoEmbedMessage.editMessageEmbeds(orderInfoEmbed.build()).queue()

            val stats = StatsDAO.get(order.supplier)
            stats.accepted++
            StatsDAO.dao.update(stats)

            val shippedOrderButton = DiscordButton(label = "Shipped", buttonStyle = ButtonStyle.SUCCESS, emoji = Emoji.fromUnicode("U+1F4E6"))
            val finishOrderButton = DiscordButton(label = "Finish", buttonStyle = ButtonStyle.SUCCESS, emoji = Emoji.fromUnicode("U+263A"))
            val revokeButton = DiscordButton(label = "Revoke", buttonStyle = ButtonStyle.DANGER, emoji = Emoji.fromUnicode("U+1F614"))

            orderInfoEmbedMessage.editMessageComponents(
                ActionRow.of(ShippedOrderButton(shippedOrderButton) , FinishOrderButton(finishOrderButton)),
                ActionRow.of(RevokeOrderButton(revokeButton))).queue()
        } else {
            hook.channel.sendMessageEmbeds(defaultEmbed("${actionSender.getMember().asMention} has accepted the deal!", Color.GREEN, "Accepted")).queue()
        }
    }
}