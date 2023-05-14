package org.vzbot.discordbot.models

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import org.vzbot.discordbot.util.Continent
import org.vzbot.discordbot.util.Country

/**
 *
 * @version 1.0
 * @author Devin Fritz
 */
@DatabaseTable(tableName = "application")
class Application {

    @DatabaseField(id = true)
    var applicant: Long = 0

    @DatabaseField
    var status: ApplicationStatus = ApplicationStatus.PENDING

    @DatabaseField
    var continent: Continent = Continent.UNKNOWN

    @DatabaseField
    var textChannelID: Long = 0

    @DatabaseField
    var filaments: String = "N/A"

    @DatabaseField
    var country: Country = Country.UNKNOWN

    @DatabaseField
    var printers: Int = 0
}

enum class ApplicationStatus() {

    PENDING,
    DECLINED,
    ACCEPTED,
}
