package org.vzbot.discordbot.models

import org.simpleyaml.configuration.file.YamlFile

class Flowchart(private val startPoint: Datapoint) {

    private fun asYML(): YamlFile {
        val yaml = YamlFile()

        val index = 0

        val pointsToAdd = mutableListOf<Datapoint>()
        pointsToAdd += startPoint
        var firstPoint = true

        while(pointsToAdd.isNotEmpty()) {
            for (currentPoint in pointsToAdd) {


                pointsToAdd -= currentPoint
                val prefix = "${currentPoint.title}."
                yaml.set("$prefix.title", currentPoint.title)

                if (firstPoint)
                    yaml.set("$prefix.first", true)
                firstPoint = false

                for (meta in currentPoint.value) {
                    yaml.set("$prefix.meta.${meta.getTitle()}", meta.getMeta())
                }

                for (point in currentPoint.nextPoints) {
                    yaml.set("$prefix.point.${point.title}", point.title)
                    pointsToAdd.add(point)
                }
            }
        }

        return yaml
    }
}

class Datapoint(val title: String, val value: List<SavedMedia>) {

    var previousPoint: Datapoint? = null
    var nextPoints: List<Datapoint> = listOf()

}