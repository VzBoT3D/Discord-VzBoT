package command.implementations

import command.Command
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Emoji
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.Button
import util.Direction
import util.Point
import util.gCodeGeneratorManager
import java.awt.Color
import kotlin.math.roundToInt

private val cmd = CommandData("gcodegenerator", "will create gcode you can use to test your 3d printer.")
    .addOption(OptionType.NUMBER,"minx", "the minimum the head should go to on x", true)
    .addOption(OptionType.NUMBER, "max_x", "the maximum the head should go to on x", true)
    .addOption(OptionType.NUMBER,"miny", "the minimum the head should go to on y", true)
    .addOption(OptionType.NUMBER, "max_y", "the maximum the head should go to on y", true)
    .addOption(OptionType.NUMBER, "iterations", "who often should the movement repeat", true)
    .addOption(OptionType.NUMBER, "start_speed", "the speed the printhead will start at in mm/s", true)
    .addOption(OptionType.NUMBER, "start_acceleration", "the acceleration the speed will start at in mm/min^2", true)
    .addOption(OptionType.NUMBER, "speed_increase", "speed increase after an iteration in mm/s", true)
    .addOption(OptionType.NUMBER, "acceleration_increase", "acceleration increase after an interation in mm/min^2", true)
    .addOption(OptionType.BOOLEAN, "reprap", "if the machine is a reprap machine. If yes it will change gcode slightly")


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
        var repRap = false

        gCodeGeneratorManager.addMember(member, Point(minx, miny), Point(minx, miny), Point(maxX, maxY))

        val embed = EmbedBuilder()
        embed.setTitle("GCode Generator")
        embed.setColor(Color.GREEN)
        embed.setDescription("**Select a movement to create a pattern.**\n")
        embed.addField("Owner", member.effectiveName, true)
        embed.addField("Printhead Position", "Current position: X:$x Y:$y\")", true)
        embed.addField("Movement Pattern", gCodeGeneratorManager.getDirections(member).joinToString { pair -> Emoji.fromUnicode(pair.first.unicode).name}, false)


        event.replyEmbeds(embed.build())
            .addActionRow(Button.secondary(Direction.RIGHT.name, Emoji.fromUnicode("U+27A1")), Button.secondary(Direction.DOWN.name, Emoji.fromUnicode("U+2B07")), Button.secondary(Direction.RIGHT_DOWN_LEFT.name, Emoji.fromUnicode("U+2198")))
            .addActionRow(Button.primary("submit", "Submit"),Button.danger("delete", "Delete last movement"), Button.danger("cancel", "Cancel")).queue { q -> (q.retrieveOriginal().queue { m -> gCodeGeneratorManager.addId(member, m.id)}) }

        /*
        event.deferReply(true).queue()



        var currentSpeed = startSpeed * 60
        var currentAcceleration = startAcceleration

        if (event.getOption("reprap") != null) {
            repRap = event.getOption("reprap")!!.asBoolean
        }

        if (iterations > 100) {
            event.replyEmbeds(defaultEmbed("Only up to 100 iterations are supported", Color.RED)).queue()
            return
        }


        val file = File("temp.txt")
        val writer = PrintWriter(file)

        writer.println("G0 X$minx Y$miny F600")
        writer.println("M204 S$startAcceleration")

        for (i in 0 until iterations) {
            writer.println("G0 X$maxX F$currentSpeed")
            writer.println("G0 Y$maxY F$currentSpeed")
            writer.println("G0 X$minx F$currentSpeed")
            writer.println("G0 Y$miny F$currentSpeed")

            writer.println("G0 X$maxX F$currentSpeed")
            writer.println("G0 Y$maxY F$currentSpeed")
            writer.println("G0 X$minx F$currentSpeed")
            writer.println("G0 Y$miny F$currentSpeed")

            writer.println("G0 X$maxX F$currentSpeed")
            writer.println("G0 Y$maxY F$currentSpeed")
            writer.println("G0 X$minx F$currentSpeed")
            writer.println("G0 Y$miny F$currentSpeed")

            writer.println("G0 X$maxX Y$maxY F$currentSpeed")
            writer.println("G0 X$minx Y$miny F$currentSpeed")

            writer.println("G0 X$maxX Y$maxY F$currentSpeed")
            writer.println("G0 X$minx Y$miny F$currentSpeed")

            writer.println("G0 X$maxX Y$maxY F$currentSpeed")
            writer.println("G0 X$minx Y$miny F$currentSpeed")

            writer.println("G0 Y$maxY F$currentSpeed")

            writer.println("G0 X$maxX Y$miny F$currentSpeed")
            writer.println("G0 X$minx Y$maxY F$currentSpeed")

            writer.println("G0 X$maxX Y$miny F$currentSpeed")
            writer.println("G0 X$minx Y$maxY F$currentSpeed")

            writer.println("G0 X$maxX Y$miny F$currentSpeed")
            writer.println("G0 X$minx Y$maxY F$currentSpeed")

            writer.println("G0 Y$miny F$currentSpeed")

            currentSpeed += speedIncrease * 60
            currentAcceleration += accelerationIncrease

            writer.println("M204 S$currentAcceleration")
            writer.flush()
        }


        val embed = EmbedBuilder()
        embed.setTitle("Result")
        embed.setColor(Color.GREEN)
        embed.addField("Starting speed", "$startSpeed mm/s", true)
        embed.addField("Starting acceleration", "$startAcceleration mm/min^2", true)
        embed.addField("Ending speed", "${currentSpeed / 60} mm/s", true)
        embed.addField("Ending Acceleration", "$currentAcceleration mm/min^2", true)
        embed.addField("Iterations", "$iterations", true)
        embed.addField("Movement", ":arrow_right: :arrow_down: :arrow_left: :arrow_up: :arrow_right: :arrow_down: :arrow_left: :arrow_up: :arrow_right: :arrow_down: :arrow_left: :arrow_up: :arrow_lower_right: :arrow_upper_left:  :arrow_lower_right: :arrow_upper_left: :arrow_lower_right: :arrow_upper_left: :arrow_down: :arrow_upper_right:  :arrow_lower_left: :arrow_upper_right:  :arrow_lower_left: :arrow_upper_right:  :arrow_lower_left:", true)

        event.hook.editOriginalEmbeds(embed.build()).addFile(file).queue()

        writer.close()
        file.delete()

         */
    }

}