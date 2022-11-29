package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.daos.DAO
import org.vzbot.discordbot.daos.SubmissionDAO
import org.vzbot.discordbot.util.FileAble
import org.vzbot.discordbot.vzbot.VzBot

val submissionDAO = SubmissionDAO(VzBot.databaseConnector.connectionSourced())

@DatabaseTable(tableName = "submissions")
class Submission: FileAble {


    @DatabaseField(generatedId = true)
    var id: Long = 0

    @DatabaseField()
    var memberID: Long = 0

    override fun fromYML(input: ConfigurationSection): Boolean {
        //useless implementation
       return true
    }

    override fun toYML(yaml: YamlFile) {
        //useless implementation
    }

    override fun getDAO(): DAO<FileAble> {
        return submissionDAO as DAO<FileAble>
    }
}