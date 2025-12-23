package com.rudra.smartworktracker.ui.screens.all_funsion

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.smartworktracker.data.local.RecentFeaturesManager
import com.rudra.smartworktracker.ui.navigation.NavigationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllFunsionViewModel(application: Application) : AndroidViewModel(application) {

    private val recentFeaturesManager = RecentFeaturesManager(application)

    private val _recentFeatures = MutableStateFlow<List<NavigationItem>>(emptyList())
    val recentFeatures: StateFlow<List<NavigationItem>> = _recentFeatures.asStateFlow()

    init {
        loadRecentFeatures()
    }

    private fun loadRecentFeatures() {
        viewModelScope.launch {
            val recentRoutes = recentFeaturesManager.getRecentFeatureRoutes()
            val allFeatures = getAllFeatures()
            _recentFeatures.value = recentRoutes.mapNotNull { route ->
                allFeatures.find { it.route == route }
            }
        }
    }

    fun onFeatureClicked(feature: NavigationItem) {
        recentFeaturesManager.addRecentFeature(feature.route)
        loadRecentFeatures() // Refresh the list
    }

    private fun getAllFeatures(): List<NavigationItem> {
        // This should be a comprehensive list of all navigation items.
        // Ideally, this would be sourced from a single, reliable place.
        return listOf(
            NavigationItem.Dashboard,
            NavigationItem.UserProfile,
            NavigationItem.AddEntry,
            NavigationItem.Reports,
            NavigationItem.Journal,
            NavigationItem.WorkTimer,
            NavigationItem.Focus,
            NavigationItem.MindfulBreak,
            NavigationItem.Habit,
            NavigationItem.Expense,
            NavigationItem.Income,
            NavigationItem.Health,
            NavigationItem.Achievements,
            NavigationItem.Wisdom,
            NavigationItem.Calendar,
            NavigationItem.Analytics,
            NavigationItem.MonthlyReport,
            NavigationItem.Calculation,
            NavigationItem.FinancialStatement,
            NavigationItem.Savings,
            NavigationItem.Loans,
            NavigationItem.EMI,
            NavigationItem.CreditCard,
            NavigationItem.Transfer,
            NavigationItem.Backup,
            NavigationItem.AllFunsion,
            NavigationItem.Settings,
            NavigationItem.Team
        )
    }
}
