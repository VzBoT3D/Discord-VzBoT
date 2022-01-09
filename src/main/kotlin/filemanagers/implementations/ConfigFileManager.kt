package filemanagers.implementations

import filemanagers.FileManager
import org.json.JSONObject
import java.io.File
import java.nio.file.Files

class ConfigFileManager(val location: File): FileManager {

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

    fun getLogChannelID(): String {
        return json.getString("logChannel")
    }

}