package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import java.util.UUID


class OrderKit {

    @DatabaseField(id = true)
    var id: UUID = UUID.randomUUID()

    @DatabaseField
    var kitName: String = "N/A"

    @DatabaseField
    var orderNr: Int = 0

}