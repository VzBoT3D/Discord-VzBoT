package vzbot

import events.BotReadyEvent
import filemanagers.implementations.ConfigFileManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import util.ChannelLogger
import java.io.File

class VzBot(val bootLocation: String) {

    init {
        println("Booting up vzbot.VzBot-Controller")
        configFileManager = ConfigFileManager(File("$bootLocation/VZBoT", "config.json"))
        configFileManager.loadFile()
        val token = configFileManager.getToken()

        if (token.isEmpty())
            error("Token must be filled")

        try {
            jda = JDABuilder.createDefault(token).build()
        }catch (e: Exception) {
            error("Invalid token or connection")
        }

        jda.addEventListener(BotReadyEvent())

    }


    companion object {
        lateinit var jda: JDA
        lateinit var discord: Guild
        lateinit var configFileManager: ConfigFileManager
        lateinit var channelLogger: ChannelLogger
    }


}