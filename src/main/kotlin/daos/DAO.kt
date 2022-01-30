package daos

import util.FileAble

interface DAO<T> {


    fun initTable()
    fun create(obj: FileAble)


}