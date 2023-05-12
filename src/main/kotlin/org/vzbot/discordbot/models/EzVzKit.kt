package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DatabaseTable(tableName = "ezvz-kit")
class EzVzKit {

    @DatabaseField(id = true)
    var name: String = ""

    @DatabaseField
    var description: String = ""

    @DatabaseField
    var url: String = ""
}
