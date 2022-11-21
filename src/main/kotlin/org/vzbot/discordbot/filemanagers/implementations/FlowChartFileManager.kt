package org.vzbot.discordbot.filemanagers.implementations

import org.json.JSONObject
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.filemanagers.FileManager
import org.vzbot.discordbot.models.*
import java.io.File
import java.nio.file.Files

class FlowChartFileManager(private val location: File): FileManager {

    private val yamlFile = YamlFile()

    override fun loadFile() {
        if (!location.exists()) {
            location.parentFile.mkdirs()

            try {
                val inputStream = javaClass.classLoader.getResourceAsStream("config.json") ?: error("config.json could not be loaded")
                Files.copy(inputStream, location.toPath())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val lines = Files.readString(location.toPath())
        yamlFile.load(lines)
    }

    override fun saveFile() {
        location.writeText(yamlFile.toString())
    }

    override fun getJson(): JSONObject {
        TODO("Not yet implemented")
    }

    fun getYaml(): YamlFile {
        return yamlFile
    }

    fun getFlowCharts(): List<Flowchart> {

        val flowCharts = mutableListOf<Flowchart>()

        for (key in yamlFile.getKeys(false)) {
            if (isFirst(key)) {

            }
        }
        return flowCharts
    }


    fun getFlowchart(key: String): Flowchart? {

        if (!hasFlowChart(key)) return null

        val startPoint = Datapoint(key, mutableListOf())

        addMetaToPoint(startPoint)

        for (nextPoint in yamlFile.getStringList("$key.point")) {
            startPoint.nextPoints += getPoint(nextPoint)
        }

        return Flowchart(startPoint)

    }
    private fun getPoint(key: String): Datapoint {

        val title = yamlFile.getString("$key.title")

        val point = Datapoint(title, mutableListOf())
        addMetaToPoint(point)


        for (nextPoint in yamlFile.getStringList("$key.point")) {
            point.nextPoints += getPoint(nextPoint)
        }

        return point
    }

    private fun addMetaToPoint(point: Datapoint) {
        if (yamlFile.contains("${point.title}.meta")) {
            for (metaKeys in yamlFile.getConfigurationSection("${point.title}.meta").getKeys(false)) {
                val value = yamlFile.getString("${point.title}.meta.$metaKeys")

                if (value.endsWith(".stl")) {
                    point.value += STLMedia(File(value))
                } else {
                    point.value += StringMedia(metaKeys, value)
                }
            }
        }
    }



    fun hasFlowChart(chart: String): Boolean {
        return yamlFile.contains(chart) && isFirst(chart)
    }


    private fun isFirst(key: String): Boolean {
        return yamlFile.contains("$key.first")
    }

}