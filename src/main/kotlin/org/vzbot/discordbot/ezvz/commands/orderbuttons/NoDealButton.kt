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
import org.vzbot.discordbot.ezvz.commands.modifyField
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

@DCButton
class NoDealButton(discordButton: DiscordButton = DiscordButton()) : PermanentDiscordButton("btn_ezvz_order_nodeal", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {
        val channelID = hook.channel.idLong

        if (!OrderDAO.isOrderChannel(channelID)) {
            actionSender.respondText("An error occurred. Inform devin yeah :( (Unknown Order Channel: 404)", true)
            return
        }

        val order = OrderDAO.getOrder(channelID)

        if (actionSender.getMember().idLong != order.buyer && actionSender.getMember().idLong != order.supplier) {
            actionSender.respondText("Only the buyer and seller can cancel this deal!", true)
            return
        }

        val member = VzBot.discord.getMemberById(order.supplier) ?: return run { actionSender.respondText("There was an issue while declining this deal", true) }

        println(member.effectiveName)
        hook.channel.asTextChannel().manager.putPermissionOverride(member, 0, 0).queue()

        order.orderState = OrderState.OPEN
        order.supplier = 0


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

        val continentRole = order.continent.getRole()
        hook.channel.asTextChannel().upsertPermissionOverride(continentRole).grant(Permission.VIEW_CHANNEL).queue()

        orderInfoEmbed.editMessageComponents(ActionRow.of(AcceptOrderButton(acceptOrder), CancelOrderButton(cancelOrder))).queue()
        orderInfoEmbed.editMessageEmbeds(infoEmbed.build()).queue()

        OrderDAO.dao.update(order)

        actionSender.respondText("You have denied the deal!", true)
        hook.channel.sendMessageEmbeds(defaultEmbed("${actionSender.getMember().asMention} has canceled the deal!", Color.RED, "Canceled")).queue()

    }

}