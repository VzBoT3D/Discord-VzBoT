package command.implementations

import LocationGetter
import command.Command
import daos.daoClassManager
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import org.simpleyaml.configuration.file.YamlFile
import systems.warnsystem.Registration
import util.FileAble
import util.defaultEmbed
import java.awt.Color
import java.io.File
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType

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
                event.replyEmbeds(defaultEmbed("File not found", Color.RED)).queue()
                return
            }

            if (!file.name.endsWith("yml")) {
                event.replyEmbeds(defaultEmbed("File must end as .yml", Color.RED)).queue()
                return
            }

            val yml = YamlFile(file)
            yml.load()

            if (!yml.contains("class")) {
                event.replyEmbeds(defaultEmbed(".yml must include class layer", Color.RED)).queue()
                return
            }

            val clazz: Class<*>

            try {
                 clazz = javaClass.classLoader.loadClass(yml.getString("class"))
            }catch (e: ClassNotFoundException) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class", Color.RED)).queue()
                return
            }

            if (clazz.constructors.size != 1) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class contructors", Color.RED)).queue()
                return
            }

            val obj = clazz.constructors[0].newInstance()

            if (!obj::class.supertypes.contains(FileAble::class.createType())) {
                event.replyEmbeds(defaultEmbed(".yml unsupported super class", Color.RED)).queue()
                return
            }

            if (obj !is FileAble) {
                event.replyEmbeds(defaultEmbed(".yml unsupported class error", Color.RED)).queue()
                return
            }


            for (i in 1 until yml.getKeys(false).size) {
                yml.set("$i.id", i)
                val success = obj.fromYML(yml.getConfigurationSection("$i"))
                if (!success) {
                    event.replyEmbeds(defaultEmbed("Error while convertig id: $i to database object", Color.RED)).queue()
                    return
                }
                obj.getDAO().create(obj)
            }

            event.replyEmbeds(defaultEmbed("Done", Color.GREEN)).queue()
        } else if (event.subcommandName == "export") {
            val clazzName = event.getOption("table")!!.asString

            if (!daoClassManager.hasClass(clazzName)) {
                event.replyEmbeds(defaultEmbed("Invalid Table not found. Available Tables: ${daoClassManager.getAllClassNames().joinToString(",")}")).queue()
                return
            }

            val clazz = daoClassManager.getClassForName(clazzName)
            val obj = clazz.createInstance()

            val file = File("$clazzName.yml")
            val temp = YamlFile(file)


            if (!file.exists()) {
                file.createNewFile()
            }

            if (obj.getDAO().listAll().isEmpty()) {
                event.replyEmbeds(defaultEmbed("Table is Empty", Color.RED))
                return
            }

            println(obj.getDAO().listAll().size)

            for (otherObj in obj.getDAO().listAll()) {
                otherObj.toYML(temp)
            }

            temp.save(file)
            event.replyEmbeds(defaultEmbed("Done!", Color.GREEN)).addFile(file).queue()
            file.delete()
        }
    }
}

