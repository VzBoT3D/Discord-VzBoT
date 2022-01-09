package command

class CommandManager(private val commands: ArrayList<Command>) {

    fun addCommand(command: Command) {
        commands.add(command)
    }

    fun getCommands(): ArrayList<Command> {
        return commands
    }
}