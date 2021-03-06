package org.vzbot.discordbot.db

import com.j256.ormlite.jdbc.DataSourceConnectionSource
import com.j256.ormlite.jdbc.db.MysqlDatabaseType
import com.j256.ormlite.support.ConnectionSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.vzbot.discordbot.vzbot.VzBot
import java.sql.Connection

class DatabaseConnector {

    val dbConfig = HikariConfig()
    lateinit var dbConnector: HikariDataSource

    init {
        dbConfig.username = VzBot.configFileManager.getSQLUser()
        dbConfig.password = VzBot.configFileManager.getSQLPassword()

        dbConfig.jdbcUrl = "jdbc:mysql://${VzBot.configFileManager.getSQLHost()}:${VzBot.configFileManager.getSQLPort()}/${VzBot.configFileManager.getSQLDB()}"
        dbConfig.driverClassName = "com.mysql.cj.jdbc.Driver"

    }

    fun connectTest(): Boolean {

        var connection: Connection?
        try {
            dbConnector =  HikariDataSource(dbConfig)
            connection = dbConnector.connection
        } catch (e: Exception) {
            e.printStackTrace()
            return false;
        }
        if (connection == null)
            return false
        connection.close()
        return true
    }

    fun connectionSourced(): ConnectionSource {
        return DataSourceConnectionSource(dbConnector, MysqlDatabaseType())
    }

}