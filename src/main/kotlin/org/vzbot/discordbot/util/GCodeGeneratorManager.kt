package org.vzbot.discordbot.util

import org.vzbot.discordbot.command.implementations.GCodeGeneratorData
import net.dv8tion.jda.api.entities.Member
import org.vzbot.discordbot.util.Direction
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

val gCodeGeneratorManager = GCodeGeneratorManager()

class GCodeGeneratorManager {

    private val data = HashMap<Member, ArrayList<Pair<Direction, Point>>>()
    private val machineData = HashMap<Member, Pair<Point, Point>>()
    private val interactionData = HashMap<Member, String>()
    private val generationData = HashMap<Member, GCodeGeneratorData>()

    fun addMember(member: Member, current: Point, min: Point, max: Point, generatorData: GCodeGeneratorData) {
        data[member] = ArrayList(listOf(Pair(Direction.HOME, current)))
        machineData[member] = Pair(min, max)
        generationData[member] = generatorData
    }

    fun addId(member: Member, id: String) {
        interactionData[member] = id
    }

    fun hasId(member: Member): Boolean {
        return interactionData.containsKey(member)
    }

    fun addDirection(direction: Direction, member: Member, to: Point) {
        if (hasMember(member))
            data[member]!!.add(Pair(direction, to))
    }

    fun getInteractionID(member: Member): String {
        return interactionData[member]!!
    }

    fun removeLastDirection(member: Member) {
        if (hasMember(member))
            data[member]!!.removeLast()
    }

    fun getDirections(member: Member): ArrayList<Pair<Direction, Point>> {
        if (data[member] != null) {
            return data[member]!!
        }
        return ArrayList()
    }

    fun machineMin(member: Member): Point {
        return machineData[member]!!.first
    }

    fun getGenerationData(member: Member): GCodeGeneratorData {
        return generationData[member]!!
    }

    fun machineMax(member: Member): Point {
        return machineData[member]!!.second
    }

    fun removeMember(member: Member) {
        data.remove(member)
        machineData.remove(member)
        interactionData.remove(member)
    }

    fun hasMember(member: Member): Boolean {
        return data.containsKey(member)
    }



}