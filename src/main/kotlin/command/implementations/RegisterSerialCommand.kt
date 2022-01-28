package command.implementations

import command.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

private val commandData = CommandData("register", "Adds a warn to user. At 3 warns the user will be punished.")

class RegisterSerialCommand: Command("register", commandData, true) {

    override fun execute(member: Member, event: SlashCommandEvent) {

    }
}