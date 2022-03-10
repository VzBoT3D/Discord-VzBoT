package org.vzbot.discordbot.systems

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.util.DAO
import org.vzbot.discordbot.filemanagers.FileAble

@DatabaseTable(tableName = "warns")
class Warn(): FileAble {

    @DatabaseField(id = true)
    lateinit var id: String

    @DatabaseField(canBeNull = false)
    var memberID: Long = 0

    @DatabaseField()
    lateinit var reason: String
    
    override fun fromYML(input: ConfigurationSection): Boolean {
        return true
    }

    override fun toYML(yml: YamlFile) {
    }

    override fun getDAO(): DAO<FileAble> {
        TODO("Not yet implemented")
    }
}