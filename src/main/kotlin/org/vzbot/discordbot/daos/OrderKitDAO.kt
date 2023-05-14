package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.EzVzKit
import org.vzbot.discordbot.models.OrderKit
import java.util.UUID

object OrderKitDAO: EntityDAO<OrderKit, UUID>(OrderKit::class.java) {

    override fun generateID(): UUID {
        var uuid = UUID.randomUUID()

        while (has(uuid)) {
            uuid = UUID.randomUUID()
        }

        return uuid
    }

    fun getKitsForOrder(orderID: Int): List<EzVzKit> {
        return dao.queryForFieldValues(mapOf(Pair("orderNr", orderID))).map { KitDAO.get(it.kitName) }
    }

    fun deleteForOrder(orderID: Int) {
        return dao.queryForFieldValues(mapOf(Pair("orderNr", orderID))).forEach { remove(it) }
    }
}