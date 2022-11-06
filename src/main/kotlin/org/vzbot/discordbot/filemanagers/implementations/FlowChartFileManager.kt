package org.vzbot.discordbot.filemanagers.implementations

import org.json.JSONObject
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.filemanagers.FileManager
import org.vzbot.discordbot.models.Flowchart
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


    fun getFlowchart(key: String): Flowchart {



    }


    private fun isFirst(key: String): Boolean {
        return yamlFile.contains("key.first")
    }

}