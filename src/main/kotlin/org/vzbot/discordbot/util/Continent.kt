package org.vzbot.discordbot.util

import net.dv8tion.jda.api.entities.Role
import org.vzbot.discordbot.vzbot.VzBot

enum class Continent(val title: String, val code: String, val roleConfigName: String) {

    UNKNOWN("N/A", "N/A", "N/A"),
    EUROPE("Europe", "EU", "printerRoleEU"),
    ASIA("Asia", "AS", "printerRoleAS"),
    AFRICA("Africa", "AF", "printerRoleAF"),
    NORTH_AMERICA("North America", "NA", "printerRoleNA"),
    SOUTH_AMERICA("South America", "SA", "printerRoleSA"),
    AUSTRALIA("Australia", "OC", "printerRoleOC");

    fun getRole(): Role {
        val roleID = VzBot.configFileManager.getJson().getString(roleConfigName)
        return VzBot.discord.getRoleById(roleID) ?: error("Error while fetching role for continent $roleConfigName")

    }


    companion object {
        fun isContinent(continent: String): Boolean {
            if (continent == "N/A") return false
            return values().any { it.title.uppercase() == continent.uppercase() } || values().any { it.code.uppercase() == continent.uppercase() }
        }

        fun getContinent(continent: String): Continent {
            return values().firstOrNull { it.title.uppercase() == continent.uppercase() } ?: values().firstOrNull { it.code.uppercase() == continent.uppercase() } ?: valueOf(continent)
        }
    }
}