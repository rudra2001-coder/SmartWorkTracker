package com.rudra.smartworktracker.data.local

import android.content.Context

class RecentFeaturesManager(context: Context) {
    private val prefs = context.getSharedPreferences("recent_features", Context.MODE_PRIVATE)
    private val key = "recent_routes"
    private val maxRecents = 10

    fun addRecentFeature(route: String) {
        val recents = getRecentFeatureRoutes().toMutableList()
        recents.remove(route) // Remove if already exists to move it to the front
        recents.add(0, route) // Add to the beginning
        val updatedRecents = recents.take(maxRecents)
        prefs.edit().putStringSet(key, updatedRecents.toSet()).apply()
    }

    fun getRecentFeatureRoutes(): List<String> {
        return prefs.getStringSet(key, emptySet())?.toList() ?: emptyList()
    }
}
