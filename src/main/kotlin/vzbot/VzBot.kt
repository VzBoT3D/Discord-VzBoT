package vzbot

import command.CommandManager
import command.implementations.*
import daos.WarnDAO
import db.DatabaseConnector
import events.BotReadyEvent
import events.MessageSendEvent
import events.SlashCommandEvent
import filemanagers.implementations.ConfigFileManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import systems.warnsystem.Registration
import util.ChannelLogger
import java.io.File

class VzBot(bootLocation: String) {

    /**
     * Initialization of the VzBoT
     * Constructor will open the connection to the bot as well to the database and load all required files
     */
    init {
        println("Booting up vzbot.VzBot-Controller")
        configFileManager = ConfigFileManager(File("$bootLocation/VZBoT", "config.json"))
        configFileManager.loadFile()
        val token = configFileManager.getToken()


        databaseConnector = DatabaseConnector()
        if (token.isEmpty())
            error("Token must be filled")

        try {
            jda = JDABuilder.createDefault(token).build()
        }catch (e: Exception) {
            error("Invalid token or connection")
        }

        jda.addEventListener(BotReadyEvent())
        jda.addEventListener(SlashCommandEvent())
        jda.addEventListener(MessageSendEvent())

        commandManager.addCommand(IOCommand())
        commandManager.addCommand(AccelCommand())
        commandManager.addCommand(StepsCommand())
        commandManager.addCommand(RegisterSerialCommand())
        commandManager.addCommand(ViewBuildCommand())
    }


    companion object {
        lateinit var jda: JDA
        lateinit var discord: Guild
        lateinit var configFileManager: ConfigFileManager
        lateinit var channelLogger: ChannelLogger
        lateinit var databaseConnector: DatabaseConnector
        var commandManager = CommandManager()
    }


}