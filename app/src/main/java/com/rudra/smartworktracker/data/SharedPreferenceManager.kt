package com.rudra.smartworktracker.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rudra.smartworktracker.model.WorkLog
import com.rudra.smartworktracker.ui.screens.team.Team

class SharedPreferenceManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("WorkLogs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveWorkLogs(workLogs: List<WorkLog>) {
        val json = gson.toJson(workLogs)
        sharedPreferences.edit().putString("work_logs", json).apply()
    }

    fun getWorkLogs(): List<WorkLog> {
        val json = sharedPreferences.getString("work_logs", null)
        return if (json != null) {
            val type = object : TypeToken<List<WorkLog>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun saveTeams(teams: List<Team>) {
        val json = gson.toJson(teams)
        sharedPreferences.edit().putString("teams", json).apply()
    }

    fun getTeams(): List<Team> {
        val json = sharedPreferences.getString("teams", null)
        return if (json != null) {
            val type = object : TypeToken<List<Team>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }
}
