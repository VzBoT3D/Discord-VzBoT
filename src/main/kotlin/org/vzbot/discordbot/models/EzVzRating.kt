package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField

class EzVzRating {

    @DatabaseField(id = true)
    var ratingID: Int = 0

    @DatabaseField
    var rated: Long = 0

    @DatabaseField
    var order: Int = 0

    @DatabaseField
    var speed: Int = 0

    @DatabaseField
    var quality: Int = 0

    @DatabaseField
    var overall: Int = 0

    @DatabaseField
    var friendliness: Int = 0

    @DatabaseField
    var comment: String = ""

}