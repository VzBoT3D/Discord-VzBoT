package org.vzbot.discordbot.daos

import com.zellerfeld.zellerbotapi.io.database.EntityDAO
import org.vzbot.discordbot.models.Application
import org.vzbot.discordbot.models.ApplicationStatus

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
object ApplicationDAO : EntityDAO<Application, Long>(Application::class.java) {

    override fun generateID(): Long {
        return 0
    }

    fun getApplicationFrom(memberID: Long): Application {
        return dao.queryForFieldValues(mapOf(Pair("applicant", memberID))).first()
    }

    fun hasApplication(textChannel: Long): Boolean {
        return dao.queryForFieldValues(mapOf(Pair("textChannelID", textChannel))).size > 0
    }

    fun getApplicationFromTextChannel(textChannel: Long): Application {
        return dao.queryForFieldValues(mapOf(Pair("textChannelID", textChannel))).first()
    }

    fun hasApplied(memberID: Long): Boolean {
        return dao.queryForFieldValues(mapOf(Pair("applicant", memberID))).size > 0
    }

    fun hasBeenAccepted(memberID: Long): Boolean {
        if (!hasApplied(memberID)) return false
        return getApplicationFrom(memberID).status == ApplicationStatus.ACCEPTED
    }
}
