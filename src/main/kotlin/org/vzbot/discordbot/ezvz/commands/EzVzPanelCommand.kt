package org.vzbot.discordbot.ezvz.commands

import com.zellerfeld.zellerbotapi.annotations.DCButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordButton
import com.zellerfeld.zellerbotapi.discord.components.DiscordModal
import com.zellerfeld.zellerbotapi.discord.components.PermanentDiscordButton
import com.zellerfeld.zellerbotapi.discord.components.commands.DiscordCommand
import com.zellerfeld.zellerbotapi.discord.components.commands.actionsenders.ActionSender
import com.zellerfeld.zellerbotapi.discord.components.commands.annotations.DCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import org.vzbot.discordbot.command.isModerator
import org.vzbot.discordbot.daos.ApplicationDAO
import org.vzbot.discordbot.models.Application
import org.vzbot.discordbot.models.ApplicationStatus
import org.vzbot.discordbot.util.Country
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.util.concurrent.TimeUnit

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DCommand("sendapplypanel", "Creates a application panel for the ezvz in this channel")
class EzVzPanelCommand : DiscordCommand() {

    override fun execute(actionSender: ActionSender) {
        if (!actionSender.getMember().isModerator()) {
            actionSender.respondText("You're lacking the permission to execute this command", userOnly = true)
            return
        }

        val embed =
            defaultEmbed(
                "Click the apply button below to apply for the EzVz **BETA** Program as a printing person. **Please read our rules before applying**",
                Color.RED,
                "EzVz BETA Program",
            )
        val applyButton = ApplyButton(
            DiscordButton(
                label = "Apply",
                buttonStyle = ButtonStyle.SUCCESS,
                emoji = Emoji.fromUnicode("U+1F91D"),
            ),
        )

        actionSender.respondEmbed(embed, userOnly = false, ActionRow.of(applyButton))
    }
}

@DCButton
class ApplyButton(discordButton: DiscordButton = DiscordButton()) :
    PermanentDiscordButton("ezvz_apply", discordButton) {

    override fun execute(actionSender: ActionSender, hook: Message) {
        if (ApplicationDAO.hasApplied(actionSender.getMember().idLong)) {
            actionSender.respondText("You have already applied to the program", userOnly = true)
            return
        }

        val country =
            ActionRow.of(
                TextInput.create("country", "Country", TextInputStyle.SHORT)
                    .setPlaceholder("From which country are you?").build(),
            )
        val continent = ActionRow.of(
            TextInput.create("continent", "Continent", TextInputStyle.SHORT)
                .setPlaceholder("From which continent are you").build(),
        )
        val printers = ActionRow.of(
            TextInput.create("printers", "Printers", TextInputStyle.SHORT)
                .setPlaceholder("How many printers can you use for EzVz?").build(),
        )

        val filaments = ActionRow.of(
            TextInput.create("filaments", "Filaments", TextInputStyle.SHORT)
                .setPlaceholder("Whats filaments are you capable of printing?").build(),
        )

        val modal = DiscordModal("EzVz Application", { sender, _, values ->
            run {
                val countryInput = values["country"]!!.asString
                val continentInput = values["continent"]!!.asString
                val printersInput = values["printers"]!!.asString.toIntOrNull()
                    ?: return@run run { sender.respondText("Please provide a number for the amount of printers") }

                val filamentsInput = values["filaments"]!!.asString

                if (printersInput < 1) return@run run { sender.respondText("You must have at least 1 printer to print for the EzVz Program") }

                val category = VzBot.discord.getCategoryById(VzBot.configFileManager.getApplicationCategory())
                val textChannel =
                    VzBot.discord.createTextChannel("application-${actionSender.getMember().effectiveName}", category)
                        .complete()

                val everyoneRole = VzBot.discord.publicRole
                textChannel.upsertPermissionOverride(everyoneRole).deny(Permission.VIEW_CHANNEL).queue()
                textChannel.upsertPermissionOverride(actionSender.getMember()).grant(Permission.VIEW_CHANNEL).queue()

                if (Country.values().map { it.countryName }.none { it == countryInput }) {
                    sender.respondText("The country you provided is not supported", userOnly = true)
                    return@run
                }

                if (Country.values().map { it.code }.none { it == countryInput }) {
                    sender.respondText("The country you provided is not supported", userOnly = true)
                    return@run
                }

                val application = Application().apply {
                    applicant = sender.getMember().idLong
                    this.country = Country.values().firstOrNull { it.countryName == countryInput } ?: Country.values()
                        .first { it.code == countryInput }
                    this.continent = continentInput
                    this.printers = printersInput
                    this.textChannelID = textChannel.idLong
                    this.filaments = filamentsInput
                }

                ApplicationDAO.add(application)

                sender.respondText(
                    userOnly = true,
                    text = "Your application has been created in ${textChannel.asMention}",
                )

                val applicationInfoEmbed = EmbedBuilder(defaultEmbed("Information about this application"))
                applicationInfoEmbed.addField("Country", countryInput, false)
                applicationInfoEmbed.addField("Continent", continentInput, false)
                applicationInfoEmbed.addField("Printers", printersInput.toString(), false)
                applicationInfoEmbed.addField("Filaments", filamentsInput, false)

                textChannel.sendMessageEmbeds(applicationInfoEmbed.build())
                    .addActionRow(DeclineApplication(), AcceptApplication()).queue()

                val infoEmbed = defaultEmbed(
                    "Welcome to your application ${actionSender.getMember().asMention}." +
                        " Please follow the rules of the application here. To begin: Send pictures of all 6 Sides of the VzCube in here.",
                    Color.YELLOW,
                    "EzVz Application",
                )

                textChannel.sendMessageEmbeds(infoEmbed).queue()
            }
        }, mutableListOf(country, continent, printers, filaments))

        actionSender.respondModal(modal)
    }
}

@DCButton
class DeclineApplication(
    discordButton: DiscordButton = DiscordButton(
        label = "Decline",
        buttonStyle = ButtonStyle.DANGER,
        emoji = Emoji.fromUnicode("U+1F645"),
    ),
) :
    PermanentDiscordButton("ezvz_accept", discordButton) {
    override fun execute(actionSender: ActionSender, hook: Message) {
        if (!actionSender.getMember().isModerator()) {
            actionSender.respondText("You're missing the permissions to do this.", userOnly = true)
            return
        }

        val channelID = hook.channel.idLong

        if (!ApplicationDAO.hasApplication(channelID)) {
            actionSender.respondText("There was an error while fetching this application", userOnly = true)
            return
        }
        val application = ApplicationDAO.getApplicationFromTextChannel(channelID)

        if (application.status != ApplicationStatus.PENDING) {
            actionSender.respondText(userOnly = true, text = "This application has already been processed")
            return
        }

        val applicantID = application.applicant
        val user = VzBot.discord.retrieveMemberById(applicantID).complete()

        if (user == null) {
            actionSender.respondText("The user has left the server. This application will be deleted in 10 seconds")
            ApplicationDAO.remove(application)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
            return
        }

        application.status = ApplicationStatus.DECLINED
        ApplicationDAO.dao.update(application)

        val declinedEmbed =
            defaultEmbed(
                "Unfortunately your application has been declined ${user.asMention}. You can delete this channel by pressing the delete button below.",
                Color.RED,
                "Declined",
            )

        actionSender.respondText("You have declined this application.", userOnly = true)
        hook.channel.sendMessageEmbeds(declinedEmbed).addActionRow(DeleteApplication()).queue()
    }
}

@DCButton
class AcceptApplication(
    discordButton: DiscordButton = DiscordButton(
        label = "Accept",
        buttonStyle = ButtonStyle.SUCCESS,
        emoji = Emoji.fromUnicode("U+2705"),
    ),
) :
    PermanentDiscordButton("ezvz_decline", discordButton) {
    override fun execute(actionSender: ActionSender, hook: Message) {
        if (!actionSender.getMember().isModerator()) {
            actionSender.respondText("You're missing the permissions to do this.", userOnly = true)
            return
        }

        val channelID = hook.channel.idLong

        if (!ApplicationDAO.hasApplication(channelID)) {
            actionSender.respondText("There was an error while fetching this application", userOnly = true)
            return
        }

        val application = ApplicationDAO.getApplicationFromTextChannel(channelID)

        val applicantID = application.applicant
        val user = VzBot.discord.retrieveMemberById(applicantID).complete()

        if (user == null) {
            actionSender.respondText("The user has left the server. This application will be deleted in 10 seconds")
            ApplicationDAO.remove(application)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
            return
        }

        application.status = ApplicationStatus.ACCEPTED
        ApplicationDAO.dao.update(application)

        val acceptedEmbed =
            defaultEmbed(
                "Congratulation your application has been accepted ${user.asMention}. You are now able to see all the content belonging to the EzVz Program.",
                Color.GREEN,
                "Accepted",
            )

        actionSender.respondText("You have accepted this application.", userOnly = true)
        hook.channel.sendMessageEmbeds(acceptedEmbed).addActionRow(DeleteApplication()).queue()
    }
}

@DCButton
class DeleteApplication(
    discordButton: DiscordButton = DiscordButton(
        label = "Delete",
        buttonStyle = ButtonStyle.DANGER,
        emoji = Emoji.fromUnicode("U+1F5D1"),
    ),
) :
    PermanentDiscordButton("ezvz_delete", discordButton) {
    override fun execute(actionSender: ActionSender, hook: Message) {
        val channelID = hook.channel.idLong

        if (!ApplicationDAO.hasApplication(channelID)) {
            actionSender.respondText("There was an error while fetching this application", userOnly = true)
            return
        }

        val application = ApplicationDAO.getApplicationFromTextChannel(channelID)
        ApplicationDAO.remove(application)

        actionSender.respondText("This channel will delete itself in 10 seconds")
        hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
    }
}
