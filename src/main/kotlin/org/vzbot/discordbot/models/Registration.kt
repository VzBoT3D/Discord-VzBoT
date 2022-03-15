package org.vzbot.discordbot.models

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.daos.DAO
import org.vzbot.discordbot.daos.RegistrationDAO
import org.vzbot.discordbot.util.FileAble
import org.vzbot.discordbot.vzbot.VzBot

val registrationDAO = RegistrationDAO(VzBot.databaseConnector.connectionSourced())

@DatabaseTable(tableName = "registrations")
class Registration: FileAble {

    @DatabaseField(generatedId = true)
    var id: Long = 0

    @DatabaseField(dataType = DataType.LONG_STRING)
    var description: String = ""

    @DatabaseField(dataType = DataType.LONG_STRING)
    var mediaURL: String = ""

    @DatabaseField()
    var country: String = ""

    @DatabaseField
    var date: String = ""

    @DatabaseField()
    var memberID: Long = 0

    override fun fromYML(input: ConfigurationSection): Boolean {
        if (!input.contains("id"))
            return false;
        id = input.getLong("id")

        if (!input.contains("discordID"))
            return false;
        memberID = input.getLong("discordID")

        if (!input.contains("description"))
            return false;
        description = input.getString("description")

        if (input.contains("picURL")) {
            mediaURL = input.getString("picURL")
        }

        if (!input.contains("country"))
            return false;
        country = input.getString("country")

        if (!input.contains("date"))
            return false;
        date = input.getString("date")
        return true
    }

    override fun toYML(yaml: YamlFile) {
        yaml.set("$id.id", id)
        yaml.set("$id.memberID", memberID)
        yaml.set("$id.description", description)
        yaml.set("$id.mediaURL", mediaURL)
        yaml.set("$id.country", country)
        yaml.set("$id.date", date)
    }

    fun getNextFreeID(): Long {
        return registrationDAO.getNextFreeID()
    }

    override fun getDAO(): DAO<FileAble> {
        return registrationDAO as DAO<FileAble>
    }
}