package util

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.*

fun defaultEmbed(message: String): MessageEmbed {

    val embedBuilder = EmbedBuilder()

    val timeIntDay = SimpleDateFormat("dd/MM/yyyy HH:mm:ss z").format(Date(System.currentTimeMillis()))

    embedBuilder.setAuthor("VzBoT")
    embedBuilder.setTitle("VzBoT Message log")
    embedBuilder.setFooter(timeIntDay)
    embedBuilder.setThumbnail("https://avatars.githubusercontent.com/u/90012124?s=400&u=3aa2a230843e9a8bd39c194e00c565d2d556081a&v=4")
    embedBuilder.setColor(Color.RED)
    embedBuilder.setDescription(message)

    return embedBuilder.build()

}