package org.vzbot.discordbot.models

import org.simpleyaml.configuration.file.YamlFile

class Flowchart(val startPoint: Datapoint) {

    fun getAllPoints(): MutableList<Datapoint> {
        val points = mutableListOf<Datapoint>()

        val pointsToIterate = mutableListOf<Datapoint>().apply { add(startPoint) }

        while (pointsToIterate.isNotEmpty()) {
            val list = mutableListOf<Datapoint>().apply { addAll(pointsToIterate) }

            for (point in list) {
                pointsToIterate.remove(point)
                points.add(point)

                pointsToIterate.addAll(point.nextPoints)
            }
        }
        return points
    }

    fun getPoint(title: String): Datapoint {
        return getAllPoints().first { it.title == title }
    }

    fun hasPoint(title: String) = getAllPoints().any {it.title == title}

    fun asYML(yaml: YamlFile): YamlFile {
        val pointsToAdd = mutableListOf<Datapoint>()
        pointsToAdd += startPoint
        var firstPoint = true

        while(pointsToAdd.isNotEmpty()) {
            val list = mutableListOf<Datapoint>()
            list.addAll(pointsToAdd)

            for (currentPoint in list) {

                val prefix = "${currentPoint.title}."
                yaml.set("$prefix.title", currentPoint.title)

                if (firstPoint)
                    yaml.set("$prefix.first", true)
                firstPoint = false

                for (meta in currentPoint.value) {
                    yaml.set("$prefix.meta.${meta.getTitle()}", meta.getMetaRaw())
                }

                val pointList = mutableListOf<String>()

                for (point in currentPoint.nextPoints) {
                    pointList.add(point.title)
                    pointsToAdd.add(point)
                }

                yaml.set("$prefix.point", pointList)


                pointsToAdd -= currentPoint
            }
        }

        return yaml
    }
}

class Datapoint(var title: String, val value: MutableList<SavedMedia<out Any>>) {

    var previousPoint: Datapoint? = null
    var nextPoints: List<Datapoint> = listOf()

}