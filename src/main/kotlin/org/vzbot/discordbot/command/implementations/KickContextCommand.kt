package org.vzbot.discordbot.command.implementations

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.command.ContextCommand
import org.vzbot.discordbot.util.KickHandler

private val kickCmdData = Commands.context(Command.Type.USER, "Kick")

class KickContextCommand: ContextCommand("Kick", kickCmdData) {

    override fun execute(executor: Member, target: Member, command: String, event: UserContextInteractionEvent) {

        val reason = TextInput.create("reason", "Reason", TextInputStyle.PARAGRAPH)
            .setMinLength(30)
            .build()
        val modal = Modal.create("kick${executor.idLong}", "Kick ${target.effectiveName}")
            .addActionRows(ActionRow.of(reason)).build()

        KickHandler.addToKick(executor, target)
        event.replyModal(modal).queue()
    }
}


