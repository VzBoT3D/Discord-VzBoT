package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField

class EzVzStats {

    @DatabaseField(id = true)
    var member: Long = 0

    @DatabaseField
    var accepted: Int = 0

    @DatabaseField
    var finished: Int = 0

    @DatabaseField
    var canceled: Int = 0

}