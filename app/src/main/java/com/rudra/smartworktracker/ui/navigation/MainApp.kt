package com.rudra.smartworktracker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rudra.smartworktracker.ui.screens.analytics.AnalyticsScreen
import com.rudra.smartworktracker.ui.screens.calendar.CalendarScreen
import com.rudra.smartworktracker.ui.screens.dashboard.DashboardScreen
import com.rudra.smartworktracker.ui.screens.settings.SettingsScreen
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme

@Composable
fun MainApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomNavigation(
                navController = navController,
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavigationItem.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(
                route = NavigationItem.Dashboard.route,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultPopEnterTransition() },
                popExitTransition = { defaultPopExitTransition() }
            ) {
                DashboardScreen(
                    onNavigateToCalendar = {
                        navController.navigate(NavigationItem.Calendar.route)
                    }
                )
            }

            composable(
                route = NavigationItem.Calendar.route,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultPopEnterTransition() },
                popExitTransition = { defaultPopExitTransition() }
            ) {
                CalendarScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = NavigationItem.Analytics.route,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultPopEnterTransition() },
                popExitTransition = { defaultPopExitTransition() }
            ) {
                AnalyticsScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(
                route = NavigationItem.Settings.route,
                enterTransition = { defaultEnterTransition() },
                exitTransition = { defaultExitTransition() },
                popEnterTransition = { defaultPopEnterTransition() },
                popExitTransition = { defaultPopExitTransition() }
            ) {
                SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultEnterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultExitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopEnterTransition() =
    slideIntoContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400)
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultPopExitTransition() =
    slideOutOfContainer(
        AnimatedContentTransitionScope.SlideDirection.Right,
        animationSpec = tween(400)
    )

@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navigationItems = listOf(
        NavigationItem.Dashboard,
        NavigationItem.Calendar,
        NavigationItem.Analytics,
        NavigationItem.Settings

    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : NavigationItem(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Dashboard
    )

    object Calendar : NavigationItem(
        route = "calendar",
        title = "Calendar",
        icon = Icons.Default.CalendarToday
    )

    object Analytics : NavigationItem(
        route = "analytics",
        title = "Analytics",
        icon = Icons.Default.Analytics
    )

    object Settings : NavigationItem(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    SmartWorkTrackerTheme {
        MainApp()
    }
}
