package org.vzbot.discordbot.util

import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.daos.DAO

interface FileAble {

    fun fromYML(input: ConfigurationSection): Boolean
    fun toYML(yaml: YamlFile)
    fun getDAO(): DAO<FileAble>

}