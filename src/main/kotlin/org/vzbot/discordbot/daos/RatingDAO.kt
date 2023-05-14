package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.EzVzRating

object RatingDAO: EntityDAO<EzVzRating, Int>(EzVzRating::class.java) {

    override fun generateID(): Int {
        var number = (0..100000).random()

        while (OrderDAO.has(number)) {
            number = (0..100000).random()
        }

        return number
    }


    fun getRatingsForMember(member: Long): List<EzVzRating> {
        return dao.toList().filter { it.rated == member }
    }

}