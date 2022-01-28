package command

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData


abstract class Command(val name: String, val commandData: CommandData) {

    abstract fun execute(member: Member, event: SlashCommandEvent)

}