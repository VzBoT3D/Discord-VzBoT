package systems.warnsystem

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import daos.DAO
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import util.FileAble

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