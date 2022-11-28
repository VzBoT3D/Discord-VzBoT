package org.vzbot.discordbot.util

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import org.vzbot.discordbot.models.Datapoint
import org.vzbot.discordbot.models.Flowchart
import java.io.File

object STLFinderManager {

    private val configuring = mutableMapOf<Member, Message>()
    private val memberChartMap = mutableMapOf<Member, Flowchart>()
    private val currentPointMap = mutableMapOf<Member, Datapoint>()
    private val uploadMap = mutableMapOf<Member, TextChannel>()
    private val fileMap = mutableMapOf<Member, MutableList<File>>()

    fun addConfiguring(member: Member, message: Message) = run { configuring[member] = message }
    fun removeConfiguring(member: Member) = configuring.remove(member)
    fun isConfiguring(member: Member): Boolean = configuring.contains(member)
    fun isConfiguring(member: Member, message: Message): Boolean = configuring.contains(member) && configuring[member] == message
    fun getMessageConfiguring(member: Member) = configuring[member]!!
    fun addChartToMember(member: Member, chart: Flowchart) {
        memberChartMap[member] = chart
        currentPointMap[member] = chart.startPoint
    }
    fun removeChartFromMember(member: Member) = memberChartMap.remove(member)
    fun getMessageFromID(messageID: String) = configuring.values.first { it.id == messageID }
    fun getChartFromMember(member: Member) = memberChartMap[member]!!
    fun setCurrentPoint(member: Member, point: Datapoint) {
        currentPointMap[member] = point
    }
    fun getCurrentPoint(member: Member): Datapoint = currentPointMap[member]!!
    fun removeCurrentPoint(member: Member) = currentPointMap.remove(member)

    fun addUploading(member: Member, textChannel: TextChannel) {
        uploadMap[member] = textChannel
    }

    fun removeUploadedFiles(member: Member) = fileMap.remove(member)

    fun removeUploading(member: Member) = uploadMap.remove(member)
    fun isUploading(member: Member) = uploadMap.contains(member)
    fun getUploadChannel(member: Member) = uploadMap[member]!!

    fun resetMember(member: Member) {
        removeUploading(member)
        removeChartFromMember(member)
        removeConfiguring(member)
        removeCurrentPoint(member)
        removeUploadedFiles(member)
    }

    fun resetChart(member: Member) {
        removeUploading(member)
        removeChartFromMember(member)
        removeCurrentPoint(member)
        removeUploadedFiles(member)
    }


}