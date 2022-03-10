package org.vzbot.discordbot.daos

import org.vzbot.discordbot.warnsystem.Registration
import org.vzbot.discordbot.warnsystem.Warn
import org.vzbot.discordbot.util.FileAble
import kotlin.reflect.KClass

val daoClassManager = DAOClassManager()
class DAOClassManager {

    private val map = HashMap<String, KClass<out FileAble>>()

    init {
        map["registrations"] = Registration::class
        map["warns"] = Warn::class
    }

    fun hasClass(className: String): Boolean {
        return map.containsKey(className)
    }

    fun getClassForName(name: String): KClass<out FileAble> {
        return map[name] as KClass<out FileAble>
    }

    fun getAllClassNames(): MutableSet<String> {
        return map.keys
    }
}