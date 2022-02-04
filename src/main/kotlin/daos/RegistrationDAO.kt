package daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.jdbc.spring.DaoFactory
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import systems.warnsystem.Registration

class RegistrationDAO(private val connectionSource: ConnectionSource): DAO<Registration> {


    private val registrationDAO: Dao<Registration, Long> = DaoFactory.createDao(connectionSource, Registration::class.java)

    override fun initTable() {
        TableUtils.createTableIfNotExists(connectionSource, Registration::class.java)
    }

    fun getNextFreeID(): Long {
        return registrationDAO.countOf() + 1
    }

    override fun create(obj: Registration) {
        registrationDAO.executeRaw("ALTER TABLE `registrations` AUTO_INCREMENT = ${listAll().size + 1};")
        registrationDAO.create(obj)
    }

    override fun get(id: Long): Registration {
        return registrationDAO.queryForId(id)
    }

    override fun listAll(): List<Registration> {
        return registrationDAO.toList()
    }

}