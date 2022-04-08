package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.daos.DAO
import org.vzbot.discordbot.daos.MessageDAO
import org.vzbot.discordbot.daos.RegistrationDAO
import org.vzbot.discordbot.util.FileAble
import org.vzbot.discordbot.vzbot.VzBot

val messageDAO = MessageDAO(VzBot.databaseConnector.connectionSourced())


@DatabaseTable(tableName = "messages")
class Message: FileAble {

    @DatabaseField(id = true)
    var id: Long = 0

    @DatabaseField
    var oID: Long = 0

    @DatabaseField
    var channel: Long = 0

    @DatabaseField
    var oChannel: Long = 0

    override fun fromYML(input: ConfigurationSection): Boolean {
        TODO("Not yet implemented")
    }

    override fun toYML(yaml: YamlFile) {
        TODO("Not yet implemented")
    }

    override fun getDAO(): DAO<FileAble> {
        TODO("Not yet implemented")
    }
}