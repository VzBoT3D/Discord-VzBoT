package org.vzbot.discordbot.events

import org.vzbot.discordbot.daos.RegistrationDAO
import org.vzbot.discordbot.daos.WarnDAO
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.vzbot.discordbot.daos.SubmissionDAO
import org.vzbot.discordbot.util.ChannelLogger
import org.vzbot.discordbot.vzbot.VzBot

class BotReadyEvent: ListenerAdapter() {

    @Override
    override fun onReady(event: ReadyEvent) {
        if (VzBot.jda.guilds.isEmpty())
            error("VzBoT is currently not joined a discord. Please let the bot join a discord and restart...")

        val test = VzBot.databaseConnector.connectTest()

        VzBot.discord = event.jda.getGuildById("829828765512106054") ?: error("Bot is not on the VZ Discord")
        VzBot.tronxyDiscord = event.jda.getGuildById("810876385848852510") ?: error("Bot is not on the VZ Discord")


        VzBot.channelLogger = ChannelLogger(VzBot.configFileManager.getLogChannelID())

        if (!test) {
            VzBot.channelLogger.sendError("Bot started\nDatabase connection: **$test**\nThere was an error while connecting to the database. The bot will not function")
            return
        }
        VzBot.channelLogger.sendSuccess("Bot started\nDatabase connection: **$test**")
        VzBot.commandManager.registerCommandsOnDiscord()


        WarnDAO(VzBot.databaseConnector.connectionSourced()).initTable()
        RegistrationDAO(VzBot.databaseConnector.connectionSourced()).initTable()
        SubmissionDAO(VzBot.databaseConnector.connectionSourced()).initTable()

    }

}