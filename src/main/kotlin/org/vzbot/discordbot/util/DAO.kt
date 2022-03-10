package org.vzbot.discordbot.util

import org.vzbot.discordbot.filemanagers.FileAble

interface DAO<T: FileAble> {


    fun initTable()
    fun create(obj: T)
    fun listAll(): List<T>
    fun get(id: Long): T

}