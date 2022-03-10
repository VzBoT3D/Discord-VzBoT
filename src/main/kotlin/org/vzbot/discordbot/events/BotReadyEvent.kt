package org.vzbot.discordbot.events

import org.vzbot.discordbot.daos.RegistrationDAO
import org.vzbot.discordbot.daos.WarnDAO
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.util.ChannelLogger
import org.vzbot.discordbot.vzbot.VzBot

class BotReadyEvent: ListenerAdapter() {

    @Override
    override fun onReady(event: ReadyEvent) {
        if (VzBot.jda.guilds.isEmpty())
            error("VzBoT is currently not joined a discord. Please let the bot join a discord and restart...")

        val test = VzBot.databaseConnector.connectTest()

        VzBot.discord = VzBot.jda.guilds[0]

        VzBot.channelLogger = ChannelLogger(VzBot.configFileManager.getLogChannelID())

        if (!test) {
            VzBot.channelLogger.sendError("Bot started\nDatabase connection: **$test**\nThere was an error while connecting to the database. The bot will not function")
            return
        }
        VzBot.channelLogger.sendSuccess("Bot started\nDatabase connection: **$test**")
        VzBot.commandManager.registerCommandsOnDiscord()


        WarnDAO(VzBot.databaseConnector.connectionSourced()).initTable()
        RegistrationDAO(VzBot.databaseConnector.connectionSourced()).initTable()

    }

}