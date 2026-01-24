package com.rudra.smartworktracker.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.smartworktracker.data.entity.SyncStatus
import com.rudra.smartworktracker.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date

/**
 * THE ONLY TypeConverter class for the entire app.
 * Rule: Centralize all converters to prevent Room conflicts and ensure consistent SQLite storage.
 */
class AppTypeConverters {
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // --- Standard Dates (java.util.Date) ---
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // --- Modern Java Time (LocalDate, LocalTime, LocalDateTime) ---
    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }
    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.format(dateFormatter)

    @TypeConverter
    fun fromLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }
    @TypeConverter
    fun localTimeToString(time: LocalTime?): String? = time?.format(timeFormatter)

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it, dateTimeFormatter) }
    @TypeConverter
    fun localDateTimeToString(dateTime: LocalDateTime?): String? = dateTime?.format(dateTimeFormatter)

    // --- Enums ---
    @TypeConverter
    fun fromSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
    @TypeConverter
    fun syncStatusToString(status: SyncStatus): String = status.name

    @TypeConverter
    fun fromExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
    @TypeConverter
    fun expenseCategoryToString(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun fromWorkType(value: String): WorkType = WorkType.valueOf(value)
    @TypeConverter
    fun workTypeToString(type: WorkType): String = type.name

    // --- Collections & Complex Objects (JSON Serialization) ---
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    @TypeConverter
    fun toStringList(list: List<String>?): String = gson.toJson(list ?: emptyList<String>())

    @TypeConverter
    fun fromIntSet(value: String?): Set<Int> {
        val type = object : TypeToken<Set<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptySet()
    }
    @TypeConverter
    fun toIntSet(set: Set<Int>?): String = gson.toJson(set ?: emptySet<Int>())

    @TypeConverter
    fun fromBreakPeriodList(value: String?): List<BreakPeriod> {
        val type = object : TypeToken<List<BreakPeriod>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }
    @TypeConverter
    fun toBreakPeriodList(list: List<BreakPeriod>?): String = gson.toJson(list ?: emptyList<BreakPeriod>())
}
