package command.implementations

import LocationGetter
import command.Command
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.simpleyaml.configuration.file.YamlFile
import util.FileAble
import util.defaultEmbed
import java.awt.Color
import java.io.File

private val importSubCommand = SubcommandData("import", "will import a file to a database table").addOption(OptionType.STRING, "file", "name of the file", true)
private val exportSubCommand = SubcommandData("export", "will export a table into a file").addOption(OptionType.STRING, "table", "name of the table", true)
private val commandData = CommandData("io", "Will execute operations on the given table or file.").addSubcommands(
    importSubCommand, exportSubCommand)


class IOCommand: Command("io", commandData, true) {
    override fun execute(member: Member, event: SlashCommandEvent) {
        if (event.subcommandName == "import") {
            val fileName = event.getOption("file")!!.asString
            val file = File(LocationGetter().getLocation(), fileName)

            if (!file.exists()) {
                event.replyEmbeds(defaultEmbed("File not found", Color.RED))
                return
            }

            if (!file.name.endsWith("yml")) {
                event.replyEmbeds(defaultEmbed("File must end as .yml", Color.RED))
                return
            }

            val yml = YamlFile(file)
            yml.load()

            if (!yml.contains("class")) {
                event.replyEmbeds(defaultEmbed(".yml must include ", Color.RED))
                return
            }

            val clazz: Class<*>

            try {
                 clazz = javaClass.classLoader.loadClass(yml.getString("class"))
            }catch (e: ClassNotFoundException) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class", Color.RED))
                return
            }

            if (clazz.constructors.size != 1) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class", Color.RED))
                return
            }

            val obj = clazz.constructors[0].newInstance()

            if (obj::class.java.superclass != FileAble::class.java) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class", Color.RED))
                return
            }

            if (obj !is FileAble) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class", Color.RED))
                return
            }


            for (i in 0 until yml.getKeys(false).size) {
                obj.fromYML(yml.getConfigurationSection("$i"))
                obj.getDAO().create(obj)
            }


        }
    }
}

