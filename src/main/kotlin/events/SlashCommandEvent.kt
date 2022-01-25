package events

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType

class SlashCommandEvent: ListenerAdapter() {

    override fun onSlashCommand(event: SlashCommandEvent) {
        if (event.guild == null)
            return
        if (event.member == null)
            return

        var cmd = event.name

        if (event.subcommandGroup != null) {
            //no subcommands
            cmd += event.subcommandGroup
            cmd = cmd + " " + event.subcommandName
        }

        cmd = "$cmd "

        val sb = StringBuilder(cmd)

        for (data in event.options) {
            if (data.type == OptionType.BOOLEAN) {
                val b = data.asBoolean
                sb.append(b).append(" ")
                continue
            }
            sb.append(data.asString).append(" ")
        }


        sb.deleteCharAt(sb.length - 1)
        cmd = sb.toString()



    }

}