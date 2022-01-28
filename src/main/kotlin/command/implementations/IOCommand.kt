package command.implementations

import command.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

private val importSubCommand = SubcommandData("import", "will import a file to a database table").addOption(OptionType.STRING, "file", "name of the file", true)
private val exportSubCommand = SubcommandData("export", "will export a table into a file").addOption(OptionType.STRING, "table", "name of the table", true)
private val commandData = CommandData("io", "Will execute operations on the given table or file.").addSubcommands(
    importSubCommand, exportSubCommand)


class IOCommand: Command("io", commandData, true) {
    override fun execute(member: Member, event: SlashCommandEvent) {
        event.reply("Success").queue()
    }
}