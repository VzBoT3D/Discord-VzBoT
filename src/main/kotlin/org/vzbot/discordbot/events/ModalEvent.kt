package org.vzbot.discordbot.events

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.util.KickHandler
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

class ModalEvent: ListenerAdapter() {

    override fun onModalInteraction(event: ModalInteractionEvent) {
        val id = event.modalId

        val member = event.member ?: return

        println("Here")

        event.reply("wtf").queue()

        if (id == "kick${member.idLong}") {

            if (!KickHandler.isKicking(member)) {
                return
            }

            val reason = event.getValue("reason")?.asString ?: return
            val target = KickHandler.getWhoShouldGetKicked(member)
            val name = target.effectiveName

            target.sendDMIfPossible(defaultEmbed("Hey you got kicked from our discord for the following reason:\n\n**$reason**\n\nIf you think this" +
                    "is an accident and you should not have been kicked, please message our moderation team.", Color.ORANGE, "Kicked!"))
            //target.kick(reason).queueAfter(2, TimeUnit.SECONDS)
            VzBot.channelLogger.sendSuccess("User ${member.effectiveName} has kicked $name because: $reason")
            //event.reply("OK!").queue()
            KickHandler.reset(member)
        }
    }
}

fun Member.sendDMIfPossible(message: MessageEmbed) {
    try {
        user.openPrivateChannel().queue { it.sendMessageEmbeds(message).queue() }
    } catch (_: Exception) {
    }
}