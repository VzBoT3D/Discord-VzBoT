package systems.warnsystem

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import util.FileAble

@DatabaseTable(tableName = "warns")
class Warn(): FileAble {

    @DatabaseField(id = true)
    lateinit var id: String

    @DatabaseField(canBeNull = false)
    var memberID: Long = 0

    @DatabaseField()
    lateinit var reason: String


    override fun fromYMLString(input: String): Boolean {
        return true
    }

    override fun toYMLString(): String {
        return ""
    }
}