package org.vzbot.discordbot.models

import java.io.File

interface SavedMedia<Media> {


    fun getTitle(): String
    fun getMeta(): Media

}

class StringMedia(private val header: String, private val metaValue: String): SavedMedia<String> {


    override fun getTitle(): String {
        return header
    }

    override fun getMeta(): String {
        return metaValue
    }

}

class STLMedia(val location: File): SavedMedia<File> {

    override fun getMeta(): File {
        return location
    }

    override fun getTitle(): String {
       return location.name
    }

}