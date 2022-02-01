package daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.jdbc.spring.DaoFactory
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import systems.warnsystem.Registration
import util.FileAble

class RegistrationDAO(private val connectionSource: ConnectionSource): DAO<Registration> {


    private val registrationDAO: Dao<Registration, Int> = DaoFactory.createDao(connectionSource, Registration::class.java)


    override fun initTable() {
        TableUtils.createTableIfNotExists(connectionSource, Registration::class.java)
    }

    override fun create(obj: Registration) {
    }

}