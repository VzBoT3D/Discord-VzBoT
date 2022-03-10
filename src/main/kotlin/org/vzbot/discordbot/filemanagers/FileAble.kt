package org.vzbot.discordbot.filemanagers

import org.simpleyaml.configuration.ConfigurationSection
import org.simpleyaml.configuration.file.YamlFile
import org.vzbot.discordbot.util.DAO

interface FileAble {

    fun fromYML(input: ConfigurationSection): Boolean
    fun toYML(yaml: YamlFile)
    fun getDAO(): DAO<FileAble>

}