package daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import systems.warnsystem.Warn

class WarnDAO(var connection: ConnectionSource): DAO {

    private val dao: Dao<Warn, String> = DaoManager.createDao(connection, Warn::class.java)

    fun getWarnsForMember(member: Long): List<Warn> {
        val queryBuilder = dao.queryBuilder()
        return queryBuilder.where().ge("memberID",member).query()
    }

    override fun initTable() {
        TableUtils.createTableIfNotExists(connection, Warn::class.java)
    }

    fun createWarn(warn: Warn) {
        dao.create(warn)
    }

    fun getWarn(id: String) {
        dao.queryForId(id)
    }

}