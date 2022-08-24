package org.vzbot.discordbot.events

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.command.CommandManager
import org.vzbot.discordbot.vzbot.VzBot

class UserContextEvent: ListenerAdapter() {

    override fun onUserContextInteraction(event: UserContextInteractionEvent) {
        VzBot.commandManager.handleInput(event.member ?: return, event.targetMember ?: return, event)
    }

}