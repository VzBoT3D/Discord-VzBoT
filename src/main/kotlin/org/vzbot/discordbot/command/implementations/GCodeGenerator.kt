package org.vzbot.discordbot.command.implementations

import org.vzbot.discordbot.command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.Button
import org.vzbot.discordbot.util.Direction
import org.vzbot.discordbot.util.Point
import org.vzbot.discordbot.util.gCodeGeneratorManager
import java.awt.Color
import kotlin.math.roundToInt

private val cmd = CommandData("gcodegenerator", "will create gcode you can use to test your 3d printer.")
    .addOption(OptionType.NUMBER,"minx", "the minimum the head should go to on x", true)
    .addOption(OptionType.NUMBER, "max_x", "the maximum the head should go to on x", true)
    .addOption(OptionType.NUMBER,"miny", "the minimum the head should go to on y", true)
    .addOption(OptionType.NUMBER, "max_y", "the maximum the head should go to on y", true)
    .addOption(OptionType.NUMBER, "iterations", "who often should the movement repeat", true)
    .addOption(OptionType.NUMBER, "start_speed", "the speed the printhead will start at in mm/s", true)
    .addOption(OptionType.NUMBER, "start_acceleration", "the acceleration the speed will start at in mm/s^2", true)
    .addOption(OptionType.NUMBER, "speed_increase", "speed increase after an iteration in mm/s", true)
    .addOption(OptionType.NUMBER, "acceleration_increase", "acceleration increase after an interation in mm/s^2", true)

data class GCodeGeneratorData(val startSpeed: Double, val startAcceleration: Double, val iterations: Int, val speedIncrease: Double, val accelerationIncrease: Double)
class Speedtest: Command("gcodegenerator", cmd, false) {

    override fun execute(member: Member, event: SlashCommandEvent) {

        val x = event.getOption("minx")!!.asDouble
        val y = event.getOption("miny")!!.asDouble

        if (gCodeGeneratorManager.hasMember(member)) {
            event.reply("You are currently already creating a pattern. Please finish it before creating another one.").queue()
            return
        }

        val minx = event.getOption("minx")!!.asDouble
        val miny = event.getOption("miny")!!.asDouble
        val maxX = event.getOption("max_x")!!.asDouble
        val maxY = event.getOption("max_y")!!.asDouble
        val speedIncrease = event.getOption("speed_increase")!!.asDouble
        val accelerationIncrease = event.getOption("acceleration_increase")!!.asDouble
        val iterations = event.getOption("iterations")!!.asDouble.roundToInt()
        val startSpeed = event.getOption("start_speed")!!.asDouble
        val startAcceleration = event.getOption("start_acceleration")!!.asDouble

        val dat = GCodeGeneratorData(startSpeed, startAcceleration, iterations, speedIncrease, accelerationIncrease)

        gCodeGeneratorManager.addMember(member, Point(minx, miny), Point(minx, miny), Point(maxX, maxY), dat)

        val embed = EmbedBuilder()
        embed.setTitle("GCode Generator")
        embed.setColor(Color.GREEN)
        embed.setDescription("**Select a movement to create a pattern.**\n")
        embed.addField("Owner", member.effectiveName, true)
        embed.addField("Printhead Position", "Current position: X:$x Y:$y\")", true)
        embed.addField("Movement Pattern", gCodeGeneratorManager.getDirections(member).joinToString { pair -> Emoji.fromUnicode(pair.first.unicode).name}, false)


        event.replyEmbeds(embed.build())
            .addActionRow(Button.secondary(Direction.RIGHT.name, Emoji.fromUnicode("U+27A1")), Button.secondary(
                Direction.UP.name, Emoji.fromUnicode(Direction.UP.unicode)), Button.secondary(
                Direction.RIGHT_UP_LEFT.name, Emoji.fromUnicode(
                    Direction.RIGHT_UP_LEFT.unicode)))
            .addActionRow(Button.primary("submit", "Submit"),Button.danger("delete", "Delete last movement"), Button.danger("cancel", "Cancel")).queue { q -> (q.retrieveOriginal().queue { m -> gCodeGeneratorManager.addId(member, m.id)}) }
    }



}