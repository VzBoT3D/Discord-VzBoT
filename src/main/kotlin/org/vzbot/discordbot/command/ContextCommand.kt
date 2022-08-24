package org.vzbot.discordbot.command

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

abstract class ContextCommand(val command: String, val cmd: CommandData) {

    abstract fun execute(executor: Member, target: Member, command: String, event: UserContextInteractionEvent)

}