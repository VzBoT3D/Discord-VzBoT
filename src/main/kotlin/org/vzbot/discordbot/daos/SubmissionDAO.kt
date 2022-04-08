package org.vzbot.discordbot.daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.jdbc.spring.DaoFactory
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import org.vzbot.discordbot.models.Submission

class SubmissionDAO(var connection: ConnectionSource): DAO<Submission>  {

    private val submissionDAO: Dao<Submission, Long> = DaoFactory.createDao(connection, Submission::class.java)


    override fun initTable() {
        TableUtils.createTableIfNotExists(connection, Submission::class.java)
    }

    override fun create(obj: Submission) {
        submissionDAO.executeRaw("ALTER TABLE `registrations` AUTO_INCREMENT = ${listAll().size + 1};")
        submissionDAO.create(obj)
    }

    override fun listAll(): List<Submission> {
        return submissionDAO.toList()
    }

    fun hasSubmission(member: Long): Boolean {
        return listAll().any {it.memberID == member}
    }

    fun hasSubmissionID(id: Long): Boolean {
        return listAll().any {it.id == id}
    }

    fun getSubmission(member: Long): Submission {
        return listAll().first {it.memberID == member}
    }

    fun getSubmissionID(id: Long): Submission {
        return listAll().first {it.id == id}
    }

    override fun get(id: Long): Submission {
        return submissionDAO.queryForId(id)
    }
}