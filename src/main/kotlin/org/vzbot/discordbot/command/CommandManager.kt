package org.vzbot.discordbot.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

class CommandManager() {

    private val commands = ArrayList<Command>()

    fun addCommand(command: Command) {
        commands.add(command)
    }

    fun getCommands(): ArrayList<Command> {
        return commands
    }

    fun registerCommandsOnDiscord() {
        val commandsVZ = VzBot.discord.updateCommands()
        for (command in commands) {
            commandsVZ.addCommands(command.commandData)
        }
        commandsVZ.queue()
    }

    fun handleInput(input: String, member: Member, event: SlashCommandEvent) {
        if (input.isEmpty())
            return

        val name = input.split(" ")[0]



        for (command in commands) {
            if (command.name == name) {
                if (command.admin) {
                    if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                        event.replyEmbeds(defaultEmbed("Missing permission", Color.RED, "Error"))
                        return
                    }
                }
                VzBot.channelLogger.sendMessage("Command sent by user ${member.asMention} '$input'")
                command.execute(member, event)
            }
        }
    }
}