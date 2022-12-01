package org.vzbot.discordbot.util

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import org.vzbot.discordbot.models.Datapoint
import org.vzbot.discordbot.models.Flowchart
import org.vzbot.discordbot.models.STLMedia

object STLFinderManager {

    private val userMessageMap = mutableMapOf<Member, Message>()
    private val userCurrentPointMap = mutableMapOf<Member, Datapoint>()
    private val userPreviousPointMap = mutableMapOf<Member, MutableList<Datapoint>>()
    private val userCurrentChartMap = mutableMapOf<Member, Flowchart>()
    private val foundFiles = mutableMapOf<Member, List<STLMedia>>()

    fun addUserSTLFinding(member: Member, message: Message) = run { userMessageMap[member] = message }

    fun hasUserSTLFinder(member: Member) = userMessageMap.containsKey(member)
    fun isUserSTLFinding(member: Member, message: Message) = userMessageMap[member] == message

    fun addUserChart(member: Member, chart: Flowchart) = run { userCurrentChartMap[member] = chart }

    fun hasUserChart(member: Member) = userCurrentChartMap.containsKey(member)
    fun getUserChart(member: Member) = userCurrentChartMap[member]!!
    fun getMessage(member: Member) = userMessageMap[member]!!
    fun removeCurrentPoint(member: Member) = userCurrentPointMap.remove(member)

    fun setFoundFiles(member: Member, files: List<STLMedia>) {
        foundFiles[member] = files
    }

    fun getFoundFiles(member: Member) = foundFiles[member]!!
    fun setCurrentPoint(member: Member, point: Datapoint) = run { userCurrentPointMap[member] = point}

    fun hasCurrentPoint(member: Member) = userCurrentPointMap.containsKey(member)

    fun addPreviousPoint(member: Member, point: Datapoint) {
        if (!userPreviousPointMap.containsKey(member)) userPreviousPointMap[member] = mutableListOf()

        userPreviousPointMap[member]!! += point
    }

    fun removePreviousPoint(member: Member, point: Datapoint) {
        userPreviousPointMap[member]!! -= point
    }

    fun getPreviousPoints(member: Member): MutableList<Datapoint> {
        return userPreviousPointMap[member] ?: mutableListOf()
    }

    fun getCurrentPoint(member: Member) = userCurrentPointMap[member]!!

    fun hasPreviousPoint(member: Member) = userPreviousPointMap.containsKey(member)

    fun reset(member: Member) {
        userMessageMap.remove(member)
        userCurrentChartMap.remove(member)
        userPreviousPointMap.remove(member)
        userCurrentChartMap.remove(member)
        foundFiles.remove(member)
    }

}