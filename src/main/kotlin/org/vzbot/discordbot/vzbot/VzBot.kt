package org.vzbot.discordbot.vzbot

import org.vzbot.discordbot.command.CommandManager
import org.vzbot.discordbot.command.implementations.*
import org.vzbot.discordbot.db.DatabaseConnector
import org.vzbot.discordbot.filemanagers.implementations.ConfigFileManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import org.vzbot.discordbot.events.*
import org.vzbot.discordbot.models.messageDAO
import org.vzbot.discordbot.util.ChannelLogger
import java.io.File


const val VERSION = "1.0.2"

class VzBot(bootLocation: String) {

    /**
     * Initialization of the VzBoT
     * Constructor will open the connection to the bot as well to the database and load all required files
     */
    init {
        println("Booting up org.vzbot.discordbot.vzbot.VzBot-Controller")
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
        jda.addEventListener(MessageDeleteEvent())
        jda.addEventListener(GCodeGeneratorButtonEvent())

        commandManager.addCommand(IOCommand())
        commandManager.addCommand(AccelCommand())
        commandManager.addCommand(StepsCommand())
        commandManager.addCommand(RegisterSerialCommand())
        commandManager.addCommand(ViewBuildCommand())
        commandManager.addCommand(CreateSubmissionCommand())
    }


    companion object {
        lateinit var tronxyDiscord: Guild
        lateinit var jda: JDA
        lateinit var discord: Guild
        lateinit var configFileManager: ConfigFileManager
        lateinit var channelLogger: ChannelLogger
        lateinit var databaseConnector: DatabaseConnector
        var commandManager = CommandManager()
    }


}