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
import org.vzbot.discordbot.daos.ApplicationDAO
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.RatingDAO
import org.vzbot.discordbot.daos.StatsDAO
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import java.awt.Color
import kotlin.math.roundToInt

@DCButton
class AcceptOrderButton(discordButton: DiscordButton = DiscordButton()) : PermanentDiscordButton("btn_ezvz_accept_order", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {
        if (!ApplicationDAO.hasBeenAccepted(actionSender.getMember().idLong)) {
            actionSender.respondText("You need to be part of the EzVz Program to accept this order", true)
            return
        }

        val textChannel = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(textChannel)) {
            actionSender.respondText("There was an error while accepting this offer. Unknown Order Channel ID", true)
            return
        }

        val order = OrderDAO.getOrder(textChannel)

        if (actionSender.getMember().idLong == order.buyer) {
            actionSender.respondText("You can not accept your own order!", true)
            return
        }

        if (order.supplier != 0L) {
            actionSender.respondText("Another member has already accepted this order.", true)
            return
        }


        val senderPermission = hook.channel.asTextChannel().upsertPermissionOverride(actionSender.getMember())
        senderPermission.grant(Permission.MESSAGE_SEND)
        senderPermission.grant(Permission.VIEW_CHANNEL)
        senderPermission.queue()

        order.orderState = OrderState.NEGOTIATION
        order.supplier = actionSender.getMember().idLong
        OrderDAO.dao.update(order)

        val orderInfoEmbed = hook.channel.retrieveMessageById(order.infoEmbedID).complete()

        val infoEmbed = EmbedBuilder(orderInfoEmbed.embeds[0])
        infoEmbed.modifyField("Status", "Processed", false)

        val cancelOrder = DiscordButton(label = "Cancel", buttonStyle = ButtonStyle.DANGER, emoji = Emoji.fromUnicode("U+1F5D1"))
        val noDealButton = DiscordButton(label = "No Deal", buttonStyle = ButtonStyle.DANGER, emoji = Emoji.fromUnicode("U+1F625"))
        val agreeButton = DiscordButton(label = "Deal", buttonStyle = ButtonStyle.SUCCESS, emoji = Emoji.fromUnicode("U+1F91D"))

        orderInfoEmbed.editMessageComponents(
            ActionRow.of(AgreeButton(agreeButton), NoDealButton(noDealButton)),
            ActionRow.of(CancelOrderButton(cancelOrder)
            )).queue()

        orderInfoEmbed.editMessageEmbeds(infoEmbed.build()).queue()

        actionSender.respondText("You have accepted this order", true)

        val continentRole = order.continent.getRole()
        hook.channel.asTextChannel().upsertPermissionOverride(continentRole).deny(Permission.VIEW_CHANNEL).queue()

        val orderEmbed = defaultEmbed("${actionSender.getMember().asMention} has accepted this order! You can now discuss further details about this order. **This channel is now hidden from the rest of the suppliers**. Please keep an eye on our rules. If there is any issue, please inform the admin team. In the following the rating of the supplier is listed", Color.GREEN, "Potential supplier found!")

        val supplierInfoEmbed = EmbedBuilder(orderEmbed)
        val ratings = RatingDAO.getRatingsForMember(actionSender.getMember().idLong)

        if (ratings.isEmpty()) {
            supplierInfoEmbed.addField("Ratings: ", "There are no ratings yet!", true)
        } else {
            supplierInfoEmbed.addField("Ratings: ", "${ratings.size}", true)

            val deliveryRating = ratings.map { it.speed }.average()
            val qualityRating = ratings.map { it.quality }.average()
            val friendlinessRating = ratings.map { it.friendliness }.average()
            val overallRating = ratings.map { it.overall }.average()

            val stats = StatsDAO.get(actionSender.getMember().idLong)

            supplierInfoEmbed.addField("Delivery Rating", getStars(deliveryRating), false)
            supplierInfoEmbed.addField("Quality Rating", getStars(qualityRating), false)
            supplierInfoEmbed.addField("Friendliness Rating", getStars(friendlinessRating), false)
            supplierInfoEmbed.addField("Overall Rating", getStars(overallRating), false)
            val canceled = stats.canceled
            val accepted = if (stats.accepted == 0) 1.0 else stats.accepted.toDouble()
            supplierInfoEmbed.addField("Finishing Rate", "${String.format("%.2f", 100 - ((canceled  / accepted) * 100))}%" , false)
        }

        hook.channel.sendMessageEmbeds(supplierInfoEmbed.build()).queue()
    }
}

private fun getStars(rating: Double): String {
    return ":star:".repeat(rating.toInt()) + if (roundToHalf(rating) % 1 == 0.5) "<:half_black_star:1107062545166110761>" + "<:black_star:1107055353801212024>".repeat(4 - rating.toInt()) else "<:black_star:1107055353801212024>".repeat(5 - rating.toInt())
}

private fun roundToHalf(number: Double): Double {
    return (number * 2).roundToInt() / 2.0
}

