package com.hse.coursework.nutrik.utils

import androidx.room.TypeConverter
import com.hse.coursework.nutrik.model.Restriction

class ConverterUtil {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    @TypeConverter
    fun fromStringSet(value: Set<String>?): String {
        return value?.joinToString(separator = ",") ?: ""
    }

    @TypeConverter
    fun toStringSet(value: String): Set<String> {
        return if (value.isEmpty()) emptySet() else value.split(",").toSortedSet()
    }

    @TypeConverter
    fun fromRestrictionList(value: List<Restriction>?): String {
        return value?.joinToString(",") { it.name.lowercase() } ?: ""
    }

    @TypeConverter
    fun toRestrictionList(value: String): List<Restriction> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",").map { Restriction.fromString(it.trim()) }
        }
    }

    companion object {
        fun fromStringToRestrictionSet(value: Set<String>?): Set<Restriction> {
            return value?.map { Restriction.fromString(it) }?.toSet() ?: emptySet()
        }
    }

}