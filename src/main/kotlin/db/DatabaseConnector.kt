package db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import vzbot.VzBot
import java.sql.Connection

class DatabaseConnector {

    val dbConfig = HikariConfig()
    lateinit var dbConnector: HikariDataSource

    init {
        dbConfig.username = VzBot.configFileManager.getSQLUser()
        dbConfig.password = VzBot.configFileManager.getSQLPassword()

        dbConfig.jdbcUrl = "jdbc:mysql://${VzBot.configFileManager.getSQLHost()}:${VzBot.configFileManager.getSQLPort()}/${VzBot.configFileManager.getSQLDB()}"
        dbConfig.driverClassName = "org.mariadb.jdbc.Driver"

    }

    fun connectTest(): Boolean {
        dbConnector =  HikariDataSource(dbConfig)

        var connection: Connection? = null
        try {
            connection = dbConnector.connection
        } catch (e: Exception) {
            return false;
        }
        if (connection == null)
            return false
        connection.close()
        return true
    }

}