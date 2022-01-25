package events

import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import util.ChannelLogger
import vzbot.VzBot

class BotReadyEvent: ListenerAdapter() {

    @Override
    override fun onReady(event: ReadyEvent) {
        if (VzBot.jda.guilds.isEmpty())
            error("VzBoT is currently not joined a discord. Please let the bot join a discord and restart...")

        val test = VzBot.databaseConnector.connectTest()

        VzBot.discord = VzBot.jda.guilds[0]

        VzBot.channelLogger = ChannelLogger(VzBot.configFileManager.getLogChannelID())
        VzBot.channelLogger.sendMessage("Bot started\nDatabase connection: $test")
    }

}