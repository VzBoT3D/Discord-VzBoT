package org.vzbot.discordbot.models

import java.io.File

interface SavedMedia<Media> {


    fun getTitle(): String
    fun getMeta(): Media

    fun getMetaRaw(): String

}

class StringMedia(private val header: String, private val metaValue: String): SavedMedia<String> {


    override fun getTitle(): String {
        return header
    }

    override fun getMeta(): String {
        return metaValue
    }

    override fun getMetaRaw(): String {
        return metaValue
    }

}

class STLMedia(private val location: File): SavedMedia<File> {

    override fun getMeta(): File {
        return location
    }

    override fun getTitle(): String {
       return location.name.replace(".","")
    }

    override fun getMetaRaw(): String {
        return location.absolutePath
    }
}

class ChartMedia(private val flowchart: Flowchart): SavedMedia<Flowchart> {
    override fun getTitle(): String {
        return flowchart.startPoint.title
    }

    override fun getMeta(): Flowchart {
        return flowchart
    }

    override fun getMetaRaw(): String {
        return flowchart.startPoint.title
    }

}