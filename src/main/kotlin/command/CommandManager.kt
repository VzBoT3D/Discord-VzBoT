package command

import java.lang.reflect.Member

class CommandManager(private val commands: ArrayList<Command>) {

    fun addCommand(command: Command) {
        commands.add(command)
    }

    fun getCommands(): ArrayList<Command> {
        return commands
    }


    fun handleInput(input: String, member: Member) {
        if (input.isEmpty())
            return

        val name = input.split(" ")[0]

        for (command in commands) {
            if (command.name == name) {
                command.execute(member)
            }
        }
    }
}