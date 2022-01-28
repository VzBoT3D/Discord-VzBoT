package daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.jdbc.spring.DaoFactory
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import systems.warnsystem.Registration

class RegistrationDAO(val connectionSource: ConnectionSource): DAO {


    private val registrationDAO: Dao<Registration, Int> = DaoFactory.createDao(connectionSource, Registration::class.java)



    override fun initTable() {
        TableUtils.createTableIfNotExists(connectionSource, Registration::class.java)
    }

    fun addRegistration(registration: Registration) {
        registrationDAO.create(registration)
    }
}