package org.vzbot.discordbot.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import org.vzbot.discordbot.command.implementations.CreateSubmissionCommand
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color

class CommandManager() {

    private val commands = ArrayList<Command>()
    private val contextCommands = ArrayList<ContextCommand>()

    fun addCommand(command: Command) {
        commands.add(command)
    }

    fun addContextCommand(contextCommand: ContextCommand) {
        contextCommands += contextCommand
    }

    fun getCommands(): ArrayList<Command> {
        return commands
    }

    fun registerCommandsOnDiscord() {
        val commandsVZ = VzBot.discord.updateCommands()
        for (command in commands) {
            commandsVZ.addCommands(command.commandData)
        }
        for (contextCommand in contextCommands) {
            commandsVZ.addCommands(contextCommand.cmd)
        }
        commandsVZ.queue()

        val commandsTronxy = VzBot.tronxyDiscord.updateCommands()
        commandsTronxy.addCommands(CreateSubmissionCommand().commandData)
        commandsTronxy.queue()
    }

    fun handleInput(clicker: Member, clicked: Member, event: UserContextInteractionEvent) {
        val name = event.name

        for (contextCommand in contextCommands) {
            if (contextCommand.command == name) {
                contextCommand.execute(clicker, clicked, name, event)
            }
        }
    }

    fun handleInput(input: String, member: Member, event: SlashCommandInteractionEvent) {
        if (input.isEmpty())
            return
        val name = input.split(" ")[0]

        for (command in commands) {
            if (command.name == name) {
                if (command.admin) {
                    if (!member.hasPermission(Permission.ADMINISTRATOR)) {
                        event.replyEmbeds(defaultEmbed("Missing permission", Color.RED, "Error")).queue()
                        return
                    }
                }
                VzBot.channelLogger.sendMessage("Command sent by user ${member.asMention} '$input'")
                command.execute(member, event)
            }
        }
    }
}