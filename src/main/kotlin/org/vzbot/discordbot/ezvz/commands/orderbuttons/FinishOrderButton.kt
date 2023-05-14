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
import org.vzbot.discordbot.daos.RatingDAO
import org.vzbot.discordbot.daos.StatsDAO
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.models.EzVzRating
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.util.concurrent.TimeUnit

@DCButton
class FinishOrderButton(discordButton: DiscordButton = DiscordButton()) : PermanentDiscordButton("btn_ezvz_finish_order", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {

        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (order.buyer != actionSender.getMember().idLong) {
            actionSender.respondText("Only the buyer can finish this order", true)
            return
        }

        val rate = DiscordButton(label = "Rate", buttonStyle = ButtonStyle.PRIMARY, emoji = Emoji.fromUnicode("U+1F9D0"))
        val delete = DiscordButton(label = "Delete", buttonStyle = ButtonStyle.DANGER, emoji = Emoji.fromUnicode("U+1F5D1"))


        val orderInfoEmbed = hook.channel.retrieveMessageById(order.infoEmbedID).complete()

        val infoEmbed = EmbedBuilder(orderInfoEmbed.embeds[0])
        infoEmbed.modifyField("Status", "Finished", false)

        orderInfoEmbed.editMessageComponents().queue()
        orderInfoEmbed.editMessageEmbeds(infoEmbed.build()).queue()


        val stats = StatsDAO.get(order.supplier)
        stats.finished++
        StatsDAO.dao.update(stats)

        order.orderState = OrderState.DONE
        OrderDAO.dao.update(order)

        val member = VzBot.discord.getMemberById(order.supplier) ?: return

        val orderNotificationChannel = VzBot.discord.getTextChannelById(VzBot.configFileManager.getOrderNotificationChannel()) ?: return run { actionSender.respondText("Error while sending notification :(") }
        orderNotificationChannel.sendMessageEmbeds(defaultEmbed("${member.asMention} the Order ${order.orderID} has been marked as finished by the buyer.", Color.GREEN, "Order Done!")).queue()

        actionSender.respondText("You have successfully finished this order. If you want to rate the provider of your parts, or report anything please click the button below. Your rating will be submitted anonymously.", true, ActionRow.of(RateButton(rate), DeleteButton(delete)))
    }

}

@DCButton
class DeleteButton(discordButton: DiscordButton = DiscordButton()): PermanentDiscordButton("btn_ezvz_offer_delete", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {

        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (order.buyer != actionSender.getMember().idLong) {
            actionSender.respondText("Only the buyer can finish this order", true)
            return
        }

        if (order.orderState != OrderState.SHIPPED) {
            actionSender.respondText("The order has not been marked as shipped. The supplier has to ship it first", true)
            return
        }

        actionSender.respondText("The channel will be deleted in 10 seconds")
        hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
    }
}

@DCButton
class RateButton(discordButton: DiscordButton = DiscordButton()): PermanentDiscordButton("btn_ezvz_offer_rate", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {
        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (order.buyer != actionSender.getMember().idLong) {
            actionSender.respondText("Only the buyer can finish this order", true)
            return
        }

        val fastDeliveryRating = TextInput.create("rate_delivery", "Delivery Speed", TextInputStyle.SHORT).setPlaceholder("From 1-5").build()
        val printQualityRating = TextInput.create("rate_quality", "Print Quality", TextInputStyle.SHORT).setPlaceholder("From 1-5").build()
        val friendlinessRating = TextInput.create("rate_friendliness", "Friendliness", TextInputStyle.SHORT).setPlaceholder("From 1-5").build()
        val overallRating = TextInput.create("rate_overall", "Overall", TextInputStyle.SHORT).setPlaceholder("From 1-5").build()
        val comment = TextInput.create("rate_comment", "Comment", TextInputStyle.PARAGRAPH).build()


        val ratingModal = DiscordModal("Rating", {sender, _, values -> run {
            val deliveryRatingInput = values["rate_delivery"]!!.asString.toIntOrNull()
            val qualityRatingInput = values["rate_quality"]!!.asString.toIntOrNull()
            val friendlinessRatingInput = values["rate_friendliness"]!!.asString.toIntOrNull()
            val overallRatingInput = values["rate_overall"]!!.asString.toIntOrNull()
            val commentInput = values["rate_comment"]!!.asString

            if (deliveryRatingInput == null || qualityRatingInput == null || friendlinessRatingInput == null || overallRatingInput == null) {
                sender.respondText("Please only use whole numbers on the rating", true)
                return@DiscordModal
            }

            if (deliveryRatingInput !in 1..5 || qualityRatingInput !in 1..5 || friendlinessRatingInput !in 1..5 || overallRatingInput !in 1..5) {
                sender.respondText("Please only use values between 1 and 5", true)
                return@DiscordModal
            }

            val rating = EzVzRating().apply {
                this.ratingID = RatingDAO.generateID()
                this.rated = order.supplier
                this.order = order.orderID
                this.speed = deliveryRatingInput
                this.quality = qualityRatingInput
                this.friendliness = friendlinessRatingInput
                this.overall = overallRatingInput
                this.comment = commentInput
            }

            RatingDAO.add(rating)
            sender.respondText("Your rating has been submitted. Thanks for taking the time. This channel will delete itself in 10 seconds.", true)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
        }}, mutableListOf(ActionRow.of(fastDeliveryRating),ActionRow.of(printQualityRating), ActionRow.of(friendlinessRating), ActionRow.of(overallRating), ActionRow.of(comment)))

        actionSender.respondModal(ratingModal)
    }
}