package command

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.lang.reflect.Member

abstract class Command(val name: String, val commandData: CommandData) {

    abstract fun execute(member: Member)

}