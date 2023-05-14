package org.vzbot.discordbot.ezvz.commands

import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordSubCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommandOption
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DSubCommand
import net.dv8tion.jda.api.entities.Member
import org.vzbot.discordbot.command.isModerator
import org.vzbot.discordbot.daos.ApplicationDAO
import org.vzbot.discordbot.vzbot.VzBot

@DCommand("ezvz", "Manage everything about the ezvz program")
class EzVzCommand: DiscordCommand() {

    @DSubCommand("changes the status of the ezvz program.")
    class status() : DiscordSubCommand() {

        @DCommandOption("changes the status to the provided boolean")
        var status: Boolean = true

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You are not allowed to do this", true)
                return
            }

            VzBot.ezVzFileManager.setEzVzStatus(status)

            if (status) {
                actionSender.respondText("You have activated the EzVz Program!", true)
            } else {
                actionSender.respondText("You have disabled EzVz Program!", true)
            }
        }
    }

    @DSubCommand("bans the given user permanently from the ezvz program")
    class ban: DiscordSubCommand() {

        @DCommandOption("member to ban from the ezvz program")
        lateinit var member: Member

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You are not allowed to do this", true)
                return
            }

            VzBot.ezVzFileManager.banMember(member)
            actionSender.respondText("You have banned ${member.asMention} from the use of the EzVz Program", true)
        }
    }

    @DSubCommand("pardons the given user permanently from the ezvz program")
    class pardon: DiscordSubCommand() {

        @DCommandOption("member to pardon from the ezvz program")
        lateinit var member: Member

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You are not allowed to do this", true)
                return
            }


            VzBot.ezVzFileManager.pardonMember(member)
            actionSender.respondText("You have pardoned ${member.asMention} from the use of the EzVz Program", true)
        }
    }
}