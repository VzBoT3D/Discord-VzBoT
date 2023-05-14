package org.vzbot.discordbot.ezvz.commands

import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordSubCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommandOption
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DSubCommand
import net.dv8tion.jda.api.EmbedBuilder
import org.vzbot.discordbot.command.isModerator
import org.vzbot.discordbot.daos.KitDAO
import org.vzbot.discordbot.models.EzVzKit
import org.vzbot.discordbot.util.defaultEmbed

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DCommand("kit", "manages kit which can be bought trough the ezvz")
class KitCommand : DiscordCommand() {

    @DSubCommand("creates a new kit")
    class create : DiscordSubCommand() {
        @DCommandOption("name of the kit")
        var name: String = ""

        @DCommandOption("short description of the kit")
        var description: String = ""

        @DCommandOption("URL of the STL sheet")
        var url: String = ""

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You're lacking the permission to do this action", userOnly = true)
                return
            }

            if (KitDAO.has(name)) {
                actionSender.respondText("There is already an Kit with this name existing", userOnly = true)
                return
            }

            val kitDescription = description
            val kitURL = url
            val kitName = name
            val kit = EzVzKit().apply { this.name = kitName; this.description = kitDescription; this.url = kitURL }

            KitDAO.add(kit)
            actionSender.respondText("You have created the kit", userOnly = true)
        }
    }

    @DSubCommand("removes an existing kit")
    class remove : DiscordSubCommand() {

        @DCommandOption("name of the kit")
        var name: String = ""

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You're lacking the permission to do this action", userOnly = true)
                return
            }

            if (!KitDAO.has(name)) {
                actionSender.respondText("That kit does not exist bruh :)", userOnly = true)
                return
            }

            KitDAO.removeById(name)
            actionSender.respondText("Hello PB, thanks for deleting the kit :)", userOnly = true)
        }
    }

    @DSubCommand("lists all existing kits")
    class listkits : DiscordSubCommand() {

        override fun execute(actionSender: ActionSender) {
            if (!actionSender.getMember().isModerator()) {
                actionSender.respondText("You're lacking the permission to do this action", userOnly = true)
                return
            }

            val kits = KitDAO.dao.toList()

            val embed = EmbedBuilder(defaultEmbed("EzVz-Kits"))

            for (kit in kits) {
                embed.addField(kit.name, kit.description, false)
            }

            actionSender.respondEmbed(embed.build(), userOnly = true)
        }
    }
}
