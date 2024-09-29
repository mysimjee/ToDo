package com.logbook.todo.database

import androidx.room.TypeConverter
import android.net.Uri
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.format(dateTimeFormatter)
    }

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    }

    @TypeConverter
    fun fromUri(value: Uri?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toUri(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromTagList(value: List<String>?): String? {
        return value?.joinToString(",") // Convert List<String> to a comma-separated String
    }

    @TypeConverter
    fun toTagList(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() } // Split the String back to List<String>
    }

}
