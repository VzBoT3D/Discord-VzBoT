package systems.warnsystem

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import daos.DAO
import daos.RegistrationDAO
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import util.FileAble
import vzbot.VzBot

@DatabaseTable(tableName = "registrations")
class Registration: FileAble {

    @DatabaseField(generatedId = true)
    var id: Int = 0

    @DatabaseField()
    var description: String = ""

    @DatabaseField()
    var mediaURL: String = ""

    @DatabaseField()
    var country: String = ""

    @DatabaseField()
    var memberID: Long = 0

    override fun fromYML(input: ConfigurationSection): Boolean {
        return true
    }

    override fun toYML(yml: YamlFile) {
    }

    override fun getDAO(): DAO<Registration> {
        return RegistrationDAO(VzBot.databaseConnector.connectionSourced())
    }
}