package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.vzbot.discordbot.util.Continent
import org.vzbot.discordbot.util.Country
import org.vzbot.discordbot.util.OrderState

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DatabaseTable(tableName = "order")
class Order {

    @DatabaseField(id = true)
    var orderID: Int = 0

    @DatabaseField()
    var buyer: Long = 0

    @DatabaseField
    var continent: Continent = Continent.UNKNOWN

    @DatabaseField
    var country: Country = Country.UNKNOWN

    @DatabaseField
    var wishedColors: String = ""

    @DatabaseField
    var orderChannel: Long = 0

    @DatabaseField
    var supplier: Long = 0

    @DatabaseField
    var infoEmbedID: Long = 0

    @DatabaseField
    var orderState: OrderState = OrderState.OPEN

    @DatabaseField
    var buyerAgreed: Boolean = false

    @DatabaseField
    var supplierAgreed: Boolean = false


}
