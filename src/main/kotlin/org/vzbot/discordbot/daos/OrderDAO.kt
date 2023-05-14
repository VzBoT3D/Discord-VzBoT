package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.Order

object OrderDAO : EntityDAO<Order, Int>(Order::class.java) {

    override fun generateID(): Int {
        var number = (0..100000).random()

        while (has(number)) {
            number = (0..100000).random()
        }

        return number
    }


    fun isOrderChannel(channelID: Long): Boolean {
        return dao.queryForFieldValues(mapOf(Pair("orderChannel", channelID))).size > 0
    }

    fun getOrder(channelID: Long): Order {
        return dao.queryForFieldValues(mapOf(Pair("orderChannel", channelID))).first()
    }

}