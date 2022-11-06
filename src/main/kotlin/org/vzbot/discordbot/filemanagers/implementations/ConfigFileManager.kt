package org.vzbot.discordbot.filemanagers.implementations

import org.vzbot.discordbot.filemanagers.FileManager
import net.dv8tion.jda.api.entities.TextChannel
import org.json.JSONObject
import org.vzbot.discordbot.vzbot.VzBot
import java.io.File
import java.nio.file.Files

class ConfigFileManager(private val location: File): FileManager {

    private lateinit var json: JSONObject

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
        json = JSONObject(lines)
    }

    override fun saveFile() {
        location.writeText(json.toString(1))
    }

    override fun getJson(): JSONObject {
        return json
    }

    fun getToken(): String {
        return json.getString("token")
    }

    fun getChannels(): List<TextChannel> {
        return json.getJSONArray("mediaOnlyChannels").map { VzBot.jda.getTextChannelById(it.toString()) ?: error("Channel in mediaBlocker not found: $it") }
    }

    fun getLogChannelID(): String {
        return json.getString("logChannel")
    }

    fun getSQLUser(): String {
        return json.getString("sql_user")
    }

    fun getSQLPassword(): String {
        return json.getString("sql_password")
    }

    fun getSQLPort(): Int {
        return json.getInt("sql_port")
    }

    fun getSQLHost(): String {
        return json.getString("sql_host")
    }

    fun getTronxySubmissionChannel(): String {
        return json.getString("submissionChannelTronxy")
    }

    fun getVZSubmissionChannel(): String {
        return json.getString("submissionChannelVZ")
    }

    fun getSQLDB(): String {
        return json.getString("sql_database")
    }

    fun getGitHubToken(): String {
        return json.getString("gitHub")
    }

    fun getSerialAnnouncementChannelID(): String {
        return json.getString("serialAnnounceChannel")
    }

}