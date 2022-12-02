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
            location.createNewFile()
        }

        yamlFile.load(location)
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
                flowCharts += getFlowchart(key) ?: continue
            }
        }
        return flowCharts
    }

    fun deleteChart(title: String) {

        val chart = getFlowchart(title)

        for (point in chart!!.getAllPoints()) {
            yamlFile.set(point.title, null)
        }

        saveFile()
    }


    fun getFlowchart(key: String): Flowchart? {

        if (!hasFlowChart(key)) return null

        val startPoint = Datapoint(key, mutableListOf())

        addMetaToPoint(startPoint, true, key)

        for (nextPoint in yamlFile.getStringList("$key.point")) {
            startPoint.nextPoints += getPoint(nextPoint, key)
        }

        return Flowchart(startPoint)

    }
    private fun getPoint(key: String, chartName: String): Datapoint {

        val title = yamlFile.getString("$chartName~$key.title")

        val point = Datapoint(title, mutableListOf())
        addMetaToPoint(point, false, chartName)

        for (nextPoint in yamlFile.getStringList("$chartName~$key.point")) {
            point.nextPoints += getPoint(nextPoint, chartName)
        }

        return point
    }

    private fun addMetaToPoint(point: Datapoint, startPoint: Boolean, chartName: String) {

        val prefix = if (startPoint) "${point.title}.meta" else "$chartName~${point.title}.meta"

        if (yamlFile.contains(prefix)) {
            for (metaKeys in yamlFile.getConfigurationSection(prefix).getKeys(false)) {
                val value = yamlFile.getString("$prefix.$metaKeys")

                try {
                    val file = File(value)

                    if (file.exists()) {
                        point.value += STLMedia(File(value))
                    } else if (hasFlowChart(value)) {
                        point.value += ChartMedia(getFlowchart(value)!!)
                    } else {
                        point.value += StringMedia(metaKeys, value)
                    }
                } catch (e: Exception) {
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