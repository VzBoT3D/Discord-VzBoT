package org.vzbot.discordbot.daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.jdbc.spring.DaoFactory
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import org.vzbot.discordbot.models.Message
import org.vzbot.discordbot.warnsystem.Registration

class MessageDAO(private val connectionSource: ConnectionSource): DAO<Message> {

    private val messageDAO: Dao<Message, Long> = DaoFactory.createDao(connectionSource, Message::class.java)


    override fun initTable() {
        TableUtils.createTableIfNotExists(connectionSource, Message::class.java)
    }

    override fun create(obj: Message) {
        messageDAO.create(obj)
    }

    fun delete(msg: Message) {
        messageDAO.delete(msg)
    }

    override fun listAll(): List<Message> {
        return messageDAO.toList()
    }

    override fun get(id: Long): Message {
        return messageDAO.queryForId(id)
    }

    fun getOId(id: Long): Message {
        return messageDAO.first {it.oID == id}
    }

    fun hasID(id: Long): Boolean {
        return messageDAO.idExists(id)
    }

    fun hasOID(id: Long): Boolean {
        return listAll().any {it.oID == id}
    }

    fun getIdOrOID(id: Long): Message {
        return if(hasID(id)) get(id) else if (hasOID(id)) getOId(id) else Message()
    }


}