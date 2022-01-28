package util

interface FileAble {

    fun fromYMLString(input: String): Boolean
    fun toYMLString(): String

}