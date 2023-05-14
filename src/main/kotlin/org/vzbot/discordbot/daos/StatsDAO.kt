package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.EzVzStats

object StatsDAO: EntityDAO<EzVzStats, Long>(EzVzStats::class.java) {

    override fun generateID(): Long {
        TODO("Not yet implemented")
    }

}