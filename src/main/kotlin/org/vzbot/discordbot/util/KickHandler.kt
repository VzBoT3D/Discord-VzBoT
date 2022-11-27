package org.vzbot.discordbot.util

import net.dv8tion.jda.api.entities.Member
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object KickHandler {

    private val kickMap = HashMap<Member, Member>()

    fun addToKick(kicker: Member, toKicked: Member) {
        kickMap[kicker] = toKicked
    }

    fun isKicking(kicker: Member): Boolean = kickMap.containsKey(kicker)

    fun getWhoShouldGetKicked(kicker: Member): Member = kickMap[kicker]!!

    fun reset(kicker: Member) = kickMap.remove(kicker)
}