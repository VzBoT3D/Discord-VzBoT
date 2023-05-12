package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.EzVzKit

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
object KitDAO : EntityDAO<EzVzKit, String>(EzVzKit::class.java) {

    override fun generateID(): String {
        return ""
    }
}
