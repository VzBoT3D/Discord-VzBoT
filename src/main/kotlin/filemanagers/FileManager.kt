package filemanagers

import org.json.JSONObject

interface FileManager {

    fun loadFile()
    fun saveFile()
    fun getJson(): JSONObject

}