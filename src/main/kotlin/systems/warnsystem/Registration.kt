package systems.warnsystem

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

@DatabaseTable(tableName = "registrations")
class Registration {

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

}