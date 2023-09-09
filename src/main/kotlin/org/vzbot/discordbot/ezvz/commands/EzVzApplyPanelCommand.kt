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
import org.vzbot.discordbot.daos.OrderDAO
import org.vzbot.discordbot.daos.StatsDAO
import org.vzbot.discordbot.models.Application
import org.vzbot.discordbot.models.ApplicationStatus
import org.vzbot.discordbot.models.EzVzStats
import org.vzbot.discordbot.util.Continent
import org.vzbot.discordbot.util.Country
import org.vzbot.discordbot.util.OrderState
import org.vzbot.discordbot.util.defaultEmbed
import org.vzbot.discordbot.vzbot.VzBot
import java.awt.Color
import java.util.concurrent.TimeUnit

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DCommand("sendapplypanel", "Creates an EzVz application panel (embed) in the current channel")
class EzVzPanelCommand : DiscordCommand() {

    override fun execute(actionSender: ActionSender) {
        if (!actionSender.getMember().isModerator()) {
            actionSender.respondText("You lack the required permissions to perform this action." + "\n" + "(not a moderator)", userOnly = true)
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
        if (!VzBot.ezVzFileManager.getEzVzStatus()) {
            actionSender.respondText("The EzVz program is currently out of order!", true)
            return
        }

        if (VzBot.ezVzFileManager.isBanned(actionSender.getMember())) {
            actionSender.respondText("You are not eligible to join the EzVz program", true)
            return
        }

        if (ApplicationDAO.hasApplied(actionSender.getMember().idLong)) {
            actionSender.respondText("You have already applied to the EzVz program", userOnly = true)
            return
        }

        if (ApplicationDAO.hasBeenAccepted(actionSender.getMember().idLong)) {
            actionSender.respondText("You are already a member of the EzVz program", userOnly = true)
            return
        }

        val country = ActionRow.of(
            TextInput.create("country", "Country", TextInputStyle.SHORT)
                .setPlaceholder("Which country are you located in?").build(),
        )
        val continent = ActionRow.of(
            TextInput.create("continent", "Continent", TextInputStyle.SHORT)
                .setPlaceholder("Which continent are you located on?").build(),
        )
        val printers = ActionRow.of(
            TextInput.create("printers", "Printers", TextInputStyle.SHORT)
                .setPlaceholder("How many printers can you use for EzVz?").build(),
        )

        val filaments = ActionRow.of(
            TextInput.create("filaments", "Filaments", TextInputStyle.SHORT)
                .setPlaceholder("What filaments are you capable of printing?").build(),
        )

        val modal = DiscordModal("EzVz Provider Application", { sender, _, values ->
            run {
                val countryInput = values["country"]!!.asString
                val continentInput = values["continent"]!!.asString
                val printersInput = values["printers"]!!.asString.toIntOrNull()
                    ?: return@run run { sender.respondText("Please provide a number for the amount of printers") }

                val filamentsInput = values["filaments"]!!.asString

                if (printersInput < 1) return@run run { sender.respondText("You must have at least 1 printer to print for the EzVz Program", userOnly = true) }

                if (!Country.hasCountry(countryInput)) {
                    sender.respondText("The country you provided is not supported. A full list can be found here: $COUNTRIES_URL", userOnly = true)
                    return@run
                }

                if (!Continent.isContinent(continentInput)) {
                    sender.respondText(
                        "The continent you provided is not supported. A full list can be found here: $CONTINENTS_URL",
                        userOnly = true
                    )
                    return@run
                }

                val category = VzBot.discord.getCategoryById(VzBot.configFileManager.getApplicationCategory())
                val textChannel =
                    VzBot.discord.createTextChannel("application-${actionSender.getMember().effectiveName}", category)
                        .complete()

                val everyoneRole = VzBot.discord.publicRole
                textChannel.upsertPermissionOverride(everyoneRole).deny(Permission.VIEW_CHANNEL).queue()
                textChannel.upsertPermissionOverride(actionSender.getMember()).grant(Permission.VIEW_CHANNEL).queue()

                val application = Application().apply {
                    applicant = sender.getMember().idLong
                    this.country = Country.getCountry(countryInput)
                    this.continent = Continent.getContinent(continentInput)
                    this.printers = printersInput
                    this.textChannelID = textChannel.idLong
                    this.filaments = filamentsInput
                }

                ApplicationDAO.add(application)

                sender.respondText(
                    userOnly = true,
                    text = "Your application has been submitted in ${textChannel.asMention}",
                )

                val applicationInfoEmbed = EmbedBuilder(defaultEmbed("Information about this application"))
                applicationInfoEmbed.addField("Country", countryInput, false)
                applicationInfoEmbed.addField("Continent", continentInput, false)
                applicationInfoEmbed.addField("Printers", printersInput.toString(), false)
                applicationInfoEmbed.addField("Filaments", filamentsInput, false)

                textChannel.sendMessageEmbeds(applicationInfoEmbed.build())
                    .addActionRow(DeclineApplication(), AcceptApplication()).queue {
                        textChannel.pinMessageById(it.id).queue()
                    }

                val infoEmbed = defaultEmbed(
                    "Welcome to your EzVz Provider application, ${actionSender.getMember().asMention}!" + 
                    "\n\n" + 
                    "Please follow the rules of the application here." +
                    "\n" + 
                    "To start off, please send pictures of all 6 sides of the requested printed VzCube in here.",
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
            actionSender.respondText("You lack the required permissions to perform this action." + "\n" + "(not a moderator)", userOnly = true)
            return
        }

        val channelID = hook.channel.idLong

        if (!ApplicationDAO.hasApplication(channelID)) {
            actionSender.respondText("There was an error while fetching this application", userOnly = true)
            return
        }
        val application = ApplicationDAO.getApplicationFromTextChannel(channelID)

        if (application.status != ApplicationStatus.PENDING) {
            actionSender.respondText(userOnly = true, text = "This application has already been processed!")
            return
        }

        val applicantID = application.applicant
        val user = VzBot.discord.retrieveMemberById(applicantID).complete()

        if (user == null) {
            actionSender.respondText("The user cannot be found or has left the server. This application will be deleted in 10 seconds")
            ApplicationDAO.remove(application)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
            return
        }

        application.status = ApplicationStatus.DECLINED
        ApplicationDAO.dao.update(application)

        val declinedEmbed =
            defaultEmbed(
                "${user.asMention}," + "\n" + 
                "Unfortunately, your EzVz Providers Application was denied." + "\n" + 
                "You can delete this channel with the delete button below.",
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
            actionSender.respondText("You lack the required permissions to perform this action." + "\n" + "(not a moderator)", userOnly = true)
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
            actionSender.respondText("The user has left the server. This application will be closed in 10 seconds")
            ApplicationDAO.remove(application)
            hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
            return
        }

        application.status = ApplicationStatus.ACCEPTED
        ApplicationDAO.dao.update(application)

        val acceptedEmbed =
            defaultEmbed(
                "Congratulations, ${user.asMention}!" + "\n" + 
                "Your EzVz Providers Application has been accepted!" + "\n" + 
                "You can now access the EzVz Program. Click on an order channel to get started ",
                Color.GREEN,
                "Accepted",
            )

        val stats = EzVzStats().apply {
            this.member = applicantID
        }

        val continentRole = application.continent.getRole()
        VzBot.discord.addRoleToMember(user, continentRole).queue()


        if (StatsDAO.has(stats.member)) {
            StatsDAO.dao.update(stats)
        } else {
            StatsDAO.add(stats)
        }

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

        actionSender.respondText("This channel will be closed in 10 seconds")
        hook.channel.delete().queueAfter(10, TimeUnit.SECONDS)
    }
}
