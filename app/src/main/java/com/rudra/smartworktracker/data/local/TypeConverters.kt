package com.rudra.smartworktracker.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.smartworktracker.model.BreakPeriod
import com.rudra.smartworktracker.model.WorkType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date

class TypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromWorkType(value: String?): WorkType? {
        return value?.let { WorkType.valueOf(it) }
    }

    @TypeConverter
    fun workTypeToString(workType: WorkType?): String? {
        return workType?.name
    }

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun fromLocalTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun localTimeToString(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun fromBreakPeriodList(value: String?): List<BreakPeriod>? {
        val listType = object : TypeToken<List<BreakPeriod>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun toBreakPeriodList(list: List<BreakPeriod>?): String? {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime?): String? {
        return dateTime?.toString()
    }
    @TypeConverter
    fun fromIntSet(value: String?): Set<Int>? {
        if (value == null) {
            return null
        }
        val type = object : TypeToken<Set<Int>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun toIntSet(set: Set<Int>?): String? {
        if (set == null) {
            return null
        }
        return Gson().toJson(set)
    }

}
