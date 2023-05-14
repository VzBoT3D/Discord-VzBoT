package org.vzbot.discordbot.vzbot

import com.zellerfeld.zellerbotapi.ZellerBot
import com.zellerfeld.zellerbotapi.app.Application
import com.zellerfeld.zellerbotapi.io.database.DataBaseHelper
import com.zellerfeld.zellerbotapi.io.database.DatabaseConfig
import com.zellerfeld.zellerbotapi.util.Token
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import org.vzbot.discordbot.command.CommandManager
import org.vzbot.discordbot.command.implementations.*
import org.vzbot.discordbot.db.DatabaseConnector
import org.vzbot.discordbot.events.*
import org.vzbot.discordbot.filemanagers.implementations.ConfigFileManager
import org.vzbot.discordbot.filemanagers.implementations.EzVzFileManager
import org.vzbot.discordbot.filemanagers.implementations.FlowChartFileManager
import org.vzbot.discordbot.util.ChannelLogger
import java.io.File

const val VERSION = "1.0.4"

class VzBot(bootLocation: String) : Application("VzBot", "org.vzbot") {

    override fun onShutdown() {
        TODO("Not yet implemented")
    }

    /**
     * Initialization of the VzBoT
     * Constructor will open the connection to the bot as well to the database and load all required files
     */
    init {
        println("Booting up org.vzbot.discordbot.vzbot.VzBot-Controller")
        configFileManager = ConfigFileManager(File("$bootLocation/VZBoT", "config.json"))
        configFileManager.loadFile()

        flowChartFileManager = FlowChartFileManager(File("$bootLocation/VZBoT/charts/charts.yml"))
        flowChartFileManager.loadFile()

        ezVzFileManager = EzVzFileManager(File("$bootLocation/VZBoT/ezvz-data.yml"))
        ezVzFileManager.loadFile()

        val token = configFileManager.getToken()

        databaseConnector = DatabaseConnector()
        if (token.isEmpty()) {
            error("Token must be filled")
        }

        val discordToken = Token(token)
        ZellerBot.startBot(discordToken, { }, { println(it) })

        ZellerBot.onReady { ready() }
    }

    private fun ready() {
        commandManager.addCommand(IOCommand())
        commandManager.addCommand(AccelCommand())
        commandManager.addCommand(StepsCommand())
        commandManager.addCommand(RegisterSerialCommand())
        commandManager.addCommand(ViewBuildCommand())
        commandManager.addCommand(CreateSubmissionCommand())
        commandManager.addCommand(Speedtest())
        commandManager.addCommand(RegistrationEdit())
        commandManager.addContextCommand(SerialContextCommand())
        commandManager.addCommand(STLConfigurationCommand())
        commandManager.addCommand(STLFinderCommand())

        jda = ZellerBot.bot ?: error("Error while getting JDA from API")
        ZellerBot.turnOnDebug()
        ZellerBot.registerApplication(this)

        val databaseConfig = DatabaseConfig(
            configFileManager.getSQLUser(),
            configFileManager.getSQLPassword(),
            configFileManager.getSQLHost(),
            configFileManager.getSQLDB(),
            configFileManager.getSQLPort(),
        )
        DataBaseHelper.loadDatabase(databaseConfig)

        jda.addEventListener(SlashCommandEvent())
        jda.addEventListener(MessageSendEvent())
        jda.addEventListener(GCodeGeneratorButtonEvent())
        jda.addEventListener(UserContextEvent())
        jda.addEventListener(STLConfigEvents())
        jda.addEventListener(STLFinderEvents())

        BotReady.onReady(jda)
    }

    companion object {
        lateinit var tronxyDiscord: Guild
        lateinit var jda: JDA
        lateinit var discord: Guild
        lateinit var configFileManager: ConfigFileManager
        lateinit var channelLogger: ChannelLogger
        lateinit var databaseConnector: DatabaseConnector
        lateinit var flowChartFileManager: FlowChartFileManager
        lateinit var ezVzFileManager: EzVzFileManager
        var commandManager = CommandManager()
    }
}
