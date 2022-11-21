package org.vzbot.discordbot

import org.vzbot.discordbot.filemanagers.implementations.FlowChartFileManager
import org.vzbot.discordbot.models.Datapoint
import org.vzbot.discordbot.models.Flowchart
import org.vzbot.discordbot.models.StringMedia
import org.vzbot.discordbot.util.Point
import org.vzbot.discordbot.vzbot.VzBot
import java.io.File
import java.net.URLDecoder

fun main() {
    VzBot(LocationGetter().getLocation().absolutePath)
}


class LocationGetter() {
    fun getLocation(): File {
        val url = javaClass.protectionDomain.codeSource.location;
        val jarFileLocation = File(url.path).parentFile;
        val path = URLDecoder.decode(jarFileLocation.absolutePath, "UTF-8");
        return File(path);
    }
}

