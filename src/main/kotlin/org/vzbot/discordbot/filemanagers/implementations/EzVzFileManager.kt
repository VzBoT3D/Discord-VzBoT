package org.vzbot.discordbot.filemanagers.implementations

import com.sun.jdi.BooleanType
import net.dv8tion.jda.api.entities.Member
import org.json.JSONObject
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.filemanagers.FileManager
import java.io.File
import java.nio.file.Files

class EzVzFileManager(private val location: File): FileManager {

    private val yamlFile = YamlFile()

    override fun loadFile() {
        if (!location.exists()) {
            location.parentFile.mkdirs()

            try {
                val inputStream =
                    javaClass.classLoader.getResourceAsStream("ezvz-data.yml") ?: error("config.json could not be loaded")
                Files.copy(inputStream, location.toPath())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        yamlFile.load(location)
    }

    override fun saveFile() {
        location.writeText(yamlFile.toString())
    }

    override fun getJson(): JSONObject {
        TODO("Not yet implemented")
    }

    fun setEzVzStatus(status: Boolean) {
        yamlFile.set("ezvz-status", status)
        saveFile()
    }

    fun getEzVzStatus(): Boolean {
        return yamlFile.getBoolean("ezvz-status")
    }

    fun banMember(member: Member) {
        if (!yamlFile.contains("banned")) {
            val list = listOf(member.id)
            yamlFile.set("banned", list)
        } else {
            val banned = yamlFile.getStringList("banned")
            banned.add(member.id)
            yamlFile.set("banned", banned)
        }
        yamlFile.save()
    }

    fun pardonMember(member: Member) {
        if (!yamlFile.contains("banned")) return
        val banned = yamlFile.getStringList("banned")
        banned.remove(member.id)
        yamlFile.set("banned", banned)
    }

    fun isBanned(member: Member): Boolean {
        return yamlFile.getStringList("banned").contains(member.id)
    }


}