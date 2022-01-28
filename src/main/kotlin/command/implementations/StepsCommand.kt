package command.implementations

import command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import java.awt.Color

private val beltSubCommand = SubcommandData("belt", "calculator for belts").addOption(OptionType.NUMBER, "teeth", "teeth of the gearing pulley", true)
    .addOption(OptionType.NUMBER, "beltpitch", "pitch of the belt gt2=2mm in mm", true)
    .addOption(OptionType.NUMBER, "gearratio", "gear ratio of the to be driven axis", true)
    .addOption(OptionType.NUMBER, "stepangle", "stepping angle of the motor in degree", true)
    .addOption(OptionType.NUMBER, "microsteps", "microstepping of the stepper", true)
private val screwSubCommand = SubcommandData("screw", "calculator for screws").addOption(OptionType.NUMBER, "pitch", "distance per rotation in mm", true)
    .addOption(OptionType.NUMBER, "microsteps", "microstepping of the stepper", true)
    .addOption(OptionType.NUMBER, "stepangle", "stepping angle of the motor in degree", true)
    .addOption(OptionType.NUMBER, "gearratio", "gear ratio of the to be driven axis", true)
private val cmd = CommandData("steps", "will calculate steps/mm and rotation distance").addSubcommands(beltSubCommand, screwSubCommand)

class StepsCommand: Command("steps", cmd, false) {
    override fun execute(member: Member, event: SlashCommandEvent) {


        if (event.subcommandName == "belt") {
            val teeth = event.getOption("teeth")!!.asDouble
            val beltpitch = event.getOption("beltpitch")!!.asDouble
            val gearratio = event.getOption("gearratio")!!.asDouble
            val stepangle = event.getOption("stepangle")!!.asDouble
            val microsteps = event.getOption("microsteps")!!.asDouble

            val stepDistance = (teeth * beltpitch * gearratio) / ((360 / stepangle) * microsteps)
            val stepsPermm = ((360 / stepangle) * microsteps) / (teeth * beltpitch * gearratio)
            val rotationDistance = teeth * beltpitch
            val fullStepsPerRotation = 360 / stepangle

            val embed = EmbedBuilder()
            embed.setTitle("Results")
            embed.setColor(Color.GREEN)

            embed.addField("Stepping distance", "$stepDistance", true)
            embed.addField("Steps per mm", "$stepsPermm", true)
            embed.addField("Rotation distance", "$rotationDistance", true)
            embed.addField("Full steps per rotation", "$fullStepsPerRotation", true)
            embed.addField("Microsteps", "$microsteps", true)
            embed.addField("Gear ration", "$gearratio", true)

            event.replyEmbeds(embed.build()).queue()
        } else if (event.subcommandName == "screw") {
            val pitch = event.getOption("pitch")!!.asDouble
            val stepangle = event.getOption("stepangle")!!.asDouble
            val microsteps = event.getOption("microsteps")!!.asDouble
            val gearratio = event.getOption("gearratio")!!.asDouble

            val stepDistance = (pitch * gearratio) / ((360 / stepangle) * microsteps)
            val stepsPermm = ((360 / stepangle) * microsteps) / (pitch * gearratio)
            val rotationDistance = pitch
            val fullStepsPerRotation = 360 / stepangle

            val embed = EmbedBuilder()
            embed.setTitle("Results")
            embed.setColor(Color.GREEN)

            embed.addField("Stepping distance", "$stepDistance", true)
            embed.addField("Steps per mm", "$stepsPermm", true)
            embed.addField("Rotation distance", "$rotationDistance", true)
            embed.addField("Full steps per rotation", "$fullStepsPerRotation", true)
            embed.addField("Microsteps", "$microsteps", true)
            embed.addField("Gear ration", "$gearratio", true)

            event.replyEmbeds(embed.build()).queue()
        }

    }
}