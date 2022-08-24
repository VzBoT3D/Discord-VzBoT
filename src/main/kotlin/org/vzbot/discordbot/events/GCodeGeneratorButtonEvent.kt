package org.vzbot.discordbot.events

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.vzbot.discordbot.util.Point
import org.vzbot.discordbot.util.Direction
import org.vzbot.discordbot.util.gCodeGeneratorManager
import java.awt.Color
import java.io.File
import java.io.PrintWriter

class GCodeGeneratorButtonEvent: ListenerAdapter() {


    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val buttonID = event.componentId
        val clicker = event.member ?: return

        if (!gCodeGeneratorManager.hasMember(clicker))  {
            event.reply("You cannot click the buttons if you are not the owner of the execution.").queue()
            return
        }

        if (!gCodeGeneratorManager.hasId(clicker)) {
            event.reply("You cannot click the buttons if you are not the owner of the execution.").queue()
            return
        }

        if (gCodeGeneratorManager.getInteractionID(clicker) != event.messageId) {
            return
        }

        val directions = gCodeGeneratorManager.getDirections(clicker)
        val lastPoint = directions.last().second
        val x = lastPoint.x
        val y = lastPoint.y

        val embed = EmbedBuilder()
        embed.setTitle("GCode Generator")
        embed.setColor(Color.GREEN)
        embed.addField("Owner", clicker.effectiveName, true)

        if (buttonID == "delete") {
            if (directions.size < 2) {
                embed.setDescription("**You cannot delete the homing pattern.**\n")
                event.editMessageEmbeds(embed.build()).queue()
                return
            }

            gCodeGeneratorManager.removeLastDirection(clicker)
            val lastPair = gCodeGeneratorManager.getDirections(clicker).last()

            val current = Point(lastPair.second.x, lastPair.second.y)
            embed.addField("Printhead Position", "Current position: X:${current.x} Y:${current.y}", true)
            embed.addField("Movement Pattern", gCodeGeneratorManager.getDirections(clicker).joinToString { pair -> Emoji.fromUnicode(pair.first.unicode).name}, false)

            val message = event.channel.retrieveMessageById(gCodeGeneratorManager.getInteractionID(clicker)).complete()
            val validDirections = availableDirections(current, gCodeGeneratorManager.machineMin(clicker), gCodeGeneratorManager.machineMax(clicker))
            event.editMessageEmbeds(embed.build()).setActionRows( listOf(ActionRow.of(validDirections.map { d -> Button.secondary(d.name, Emoji.fromUnicode(d.unicode))}), ActionRow.of(Button.primary("submit", "Submit"),Button.danger("delete", "Delete last movement"), Button.danger("cancel", "Cancel")) )).queue()
            return
        }

        if (buttonID == "submit") {
            event.deferEdit().queue()
            val dat = gCodeGeneratorManager.getGenerationData(clicker)
            val directions = gCodeGeneratorManager.getDirections(clicker)
            val minMachine = gCodeGeneratorManager.machineMin(clicker)

            var speed = dat.startSpeed
            var acc = dat.startAcceleration

            val file = File("temp.gcode")
            val writer = PrintWriter(file)

            writer.println("M204 S$acc")
            writer.println("G28")
            writer.println("G0 Z25")
            writer.println("G90")
            writer.println("G0 X${minMachine.x} Y${minMachine.y}")

            for (i in 0 until dat.iterations) {
                for (directionPair in directions) {
                    val direction = directionPair.first
                    val to = directionPair.second

                    if (direction == Direction.HOME)
                        continue

                    writer.println(direction.asGcode(to, gCodeGeneratorManager.machineMin(clicker), gCodeGeneratorManager.machineMax(clicker), speed))
                }

                speed += dat.speedIncrease
                acc += dat.accelerationIncrease
                writer.println("M204 S$acc")
            }
            val embedBuilder = EmbedBuilder()
            embedBuilder.setTitle("Finished")
            embedBuilder.setColor(Color.GREEN)
            embed.addField("Movement Pattern", gCodeGeneratorManager.getDirections(clicker).joinToString { pair -> Emoji.fromUnicode(pair.first.unicode).name}, false)


            event.hook.editOriginalEmbeds(embedBuilder.build()).setActionRows().addFile(file).queue()

            gCodeGeneratorManager.removeMember(clicker)

            writer.close()
            file.delete()

            return
        }

        if (buttonID == "cancel") {
            gCodeGeneratorManager.removeMember(clicker)
            embed.setDescription("**The process has been canceled..**\n")
            event.editMessageEmbeds(embed.build()).queue()
            event.hook.deleteOriginal().queue()
            return
        }

        if (event.button?.emoji == null) {
            return
        }


        val direction = Direction.values().first { v -> v.name == buttonID}
        var current = Point(x, y)
        current = direction.change(current, gCodeGeneratorManager.machineMin(clicker), gCodeGeneratorManager.machineMax(clicker))

        embed.addField("Printhead Position", "Current position: X:${current.x} Y:${current.y}", true)

        gCodeGeneratorManager.addDirection(direction, clicker, current)
        embed.addField("Movement Pattern", gCodeGeneratorManager.getDirections(clicker).joinToString { pair -> Emoji.fromUnicode(pair.first.unicode).name}, false)

        val message = event.channel.retrieveMessageById(gCodeGeneratorManager.getInteractionID(clicker)).complete()
        val validDirections = availableDirections(current, gCodeGeneratorManager.machineMin(clicker), gCodeGeneratorManager.machineMax(clicker))


        event.editMessageEmbeds(embed.build()).setActionRows( listOf(ActionRow.of(validDirections.map { d -> Button.secondary(d.name, Emoji.fromUnicode(d.unicode))}), ActionRow.of(Button.primary("submit", "Submit"),Button.danger("delete", "Delete last movement"), Button.danger("cancel", "Cancel")) )).queue()
    }

    private fun availableDirections(current: Point, min: Point, max: Point): List<Direction> {
        val directions = ArrayList<Direction>()
        if (current.x == min.x && current.y == min.y) {
            directions.add(Direction.RIGHT)
            directions.add(Direction.UP)
            directions.add(Direction.RIGHT_UP_LEFT)
        } else if (current.x == max.x && current.y == min.y) {
            directions.add(Direction.LEFT)
            directions.add(Direction.UP)
            directions.add(Direction.LEFT_UP_RIGHT)
        } else if (current.x == min.x && current.y == max.y) {
            directions.add(Direction.RIGHT)
            directions.add(Direction.DOWN)
            directions.add(Direction.RIGHT_DOWN_LEFT)
        } else if (current.x == max.x && current.y == max.y) {
            directions.add(Direction.LEFT)
            directions.add(Direction.DOWN)
            directions.add(Direction.LEFT_DOWN_RIGHT)
        }
        return directions
    }
}