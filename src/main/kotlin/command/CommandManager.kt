package command

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import vzbot.VzBot

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
                VzBot.channelLogger.sendMessage("Command sent by user ${member.asMention} '$input'")
                command.execute(member, event)
            }
        }
    }
}