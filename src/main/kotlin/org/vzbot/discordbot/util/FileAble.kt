package org.vzbot.discordbot.util

import org.vzbot.discordbot.daos.DAO
import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile

interface FileAble {

    fun fromYML(input: ConfigurationSection): Boolean
    fun toYML(yaml: YamlFile)
    fun getDAO(): DAO<FileAble>

}