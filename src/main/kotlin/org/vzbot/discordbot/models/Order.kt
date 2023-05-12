package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DatabaseTable(tableName = "order")
class Order {

    @DatabaseField(id = true)
    var buyer: Long = 0

    @DatabaseField
    var continent: String = ""

    @DatabaseField
    var country: String = ""

    @DatabaseField
    var wishedColors: String = ""

    @DatabaseField
    var orderChannel: Long = 0

    @DatabaseField
    var supplier: Long = 0
}
