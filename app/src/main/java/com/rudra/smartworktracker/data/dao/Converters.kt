package com.rudra.smartworktracker.data.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.smartworktracker.data.entity.DayOfWeek
import com.rudra.smartworktracker.data.entity.MonthlyDayOption
import java.time.LocalDate

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",") ?: emptyList()
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun fromDayOfWeekList(value: String?): List<DayOfWeek> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<DayOfWeek>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toDayOfWeekList(list: List<DayOfWeek>?): String = gson.toJson(list ?: emptyList<DayOfWeek>())

    @TypeConverter
    fun fromIntList(value: String?): List<Int> {
        if (value == null) return emptyList()
        val type = object : TypeToken<List<Int>>() {}.type
        return try {
            gson.fromJson(value, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun toIntList(list: List<Int>?): String = gson.toJson(list ?: emptyList<Int>())

    @TypeConverter
    fun fromMonthlyDayOption(value: String?): MonthlyDayOption {
        return try {
            value?.let { MonthlyDayOption.valueOf(it) } ?: MonthlyDayOption.DAY_OF_MONTH
        } catch (e: Exception) {
            MonthlyDayOption.DAY_OF_MONTH
        }
    }

    @TypeConverter
    fun toMonthlyDayOption(option: MonthlyDayOption?): String = option?.name ?: MonthlyDayOption.DAY_OF_MONTH.name
}
