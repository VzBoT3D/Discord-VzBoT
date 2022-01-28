package systems.warnsystem

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "warns")
class Warn(){

    @DatabaseField(id = true)
    lateinit var id: String

    @DatabaseField(canBeNull = false)
    var memberID: Long = 0

    @DatabaseField()
    lateinit var reason: String

}