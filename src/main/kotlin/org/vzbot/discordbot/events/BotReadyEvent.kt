package org.vzbot.discordbot.events

import net.dv8tion.jda.api.JDA
import org.vzbot.discordbot.daos.MessageDAO
import org.vzbot.discordbot.daos.RegistrationDAO
import org.vzbot.discordbot.daos.SubmissionDAO
import org.vzbot.discordbot.util.ChannelLogger
import org.vzbot.discordbot.vzbot.VERSION
import org.vzbot.discordbot.vzbot.VzBot

object BotReady {

    fun onReady(jda: JDA) {
        if (VzBot.jda.guilds.isEmpty()) {
            error("VzBoT is currently not joined a discord. Please let the bot join a discord and restart...")
        }

        val test = VzBot.databaseConnector.connectTest()

        VzBot.discord = jda.getGuildById("829828765512106054") ?: error("Bot is not on the VZ Discord")

        VzBot.channelLogger = ChannelLogger(VzBot.configFileManager.getLogChannelID())

        if (!test) {
            VzBot.channelLogger.sendError("Bot started\nVersion: $VERSION\nDatabase connection: **$test**\nThere was an error while connecting to the database. The bot will not function")
            return
        }
        VzBot.channelLogger.sendSuccess("Bot started\nDatabase connection: **$test**")
        //VzBot.commandManager.registerCommandsOnDiscord()

        RegistrationDAO(VzBot.databaseConnector.connectionSourced()).initTable()
        SubmissionDAO(VzBot.databaseConnector.connectionSourced()).initTable()
        MessageDAO(VzBot.databaseConnector.connectionSourced()).initTable()
    }
}
