package com.rudra.smartworktracker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rudra.smartworktracker.ui.screens.achievements.AchievementsScreen
import com.rudra.smartworktracker.ui.screens.add_entry.AddEntryScreen
import com.rudra.smartworktracker.ui.screens.analytics.AnalyticsScreen
import com.rudra.smartworktracker.ui.screens.all_funsion.AllFunsionScreen
import com.rudra.smartworktracker.ui.screens.backup.BackupScreen
import com.rudra.smartworktracker.ui.screens.breaks.MindfulBreakScreen
import com.rudra.smartworktracker.ui.screens.calculation.CalculationScreen
import com.rudra.smartworktracker.ui.screens.calendar.CalendarScreen
import com.rudra.smartworktracker.ui.screens.dashboard.DashboardScreen
import com.rudra.smartworktracker.ui.screens.expense.ExpenseScreen
import com.rudra.smartworktracker.ui.screens.focus.FocusScreen
import com.rudra.smartworktracker.ui.screens.habit.HabitScreen
import com.rudra.smartworktracker.ui.screens.health.HealthMetricsScreen
import com.rudra.smartworktracker.ui.screens.income.IncomeScreen
import com.rudra.smartworktracker.ui.screens.journal.DailyJournalScreen
import com.rudra.smartworktracker.ui.screens.report.MonthlyReportScreen
import com.rudra.smartworktracker.ui.screens.reports.ReportsScreen
import com.rudra.smartworktracker.ui.screens.settings.SettingsScreen
import com.rudra.smartworktracker.ui.screens.timer.WorkTimerScreen
import com.rudra.smartworktracker.ui.screens.user_profile.UserProfileScreen
import com.rudra.smartworktracker.ui.screens.user_profile.UserProfileViewModelFactory
import com.rudra.smartworktracker.ui.screens.wisdom.WisdomScreen
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = listOf(
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
        NavigationItem.Backup,
        NavigationItem.AllFunsion,
        NavigationItem.Settings
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(12.dp))
                navigationItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = item.route == currentRoute,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Smart Work Tracker") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
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
                        onNavigateToAddEntry = {
                            navController.navigate(NavigationItem.AddEntry.route)
                        },
                        onNavigateToAllFunsion = {
                            navController.navigate(NavigationItem.AllFunsion.route)
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
                    CalendarScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToEditEntry = { workLogId ->
                            navController.navigate("${NavigationItem.AddEntry.route}?workLogId=$workLogId")
                        }
                    )
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
                    SettingsScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.MonthlyReport.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    MonthlyReportScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.WorkTimer.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    WorkTimerScreen()
                }

                composable(
                    route = NavigationItem.Expense.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    ExpenseScreen()
                }
                 composable(
                    route = NavigationItem.Income.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    IncomeScreen()
                }


                composable(
                    route = NavigationItem.Health.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    HealthMetricsScreen()
                }

                composable(
                    route = NavigationItem.Focus.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    FocusScreen()
                }

                composable(
                    route = NavigationItem.Habit.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    HabitScreen()
                }

                composable(
                    route = NavigationItem.Achievements.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AchievementsScreen()
                }

                composable(
                    route = NavigationItem.Journal.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    DailyJournalScreen()
                }

                composable(
                    route = NavigationItem.MindfulBreak.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    MindfulBreakScreen()
                }

                composable(
                    route = NavigationItem.Wisdom.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    WisdomScreen()
                }



                composable(
                    route = NavigationItem.Calculation.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    CalculationScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Backup.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    BackupScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.AllFunsion.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AllFunsionScreen(navController = navController)
                }

                composable(
                    route = NavigationItem.UserProfile.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    val context = LocalContext.current
                    UserProfileScreen(
                        viewModel = viewModel(factory = UserProfileViewModelFactory(context)),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavigationItem.AddEntry.route + "?workLogId={workLogId}",
                    arguments = listOf(navArgument("workLogId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }),
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    AddEntryScreen(onNavigateBack = { navController.popBackStack() })
                }

                composable(
                    route = NavigationItem.Reports.route,
                    enterTransition = { defaultEnterTransition() },
                    exitTransition = { defaultExitTransition() },
                    popEnterTransition = { defaultPopEnterTransition() },
                    popExitTransition = { defaultPopExitTransition() }
                ) {
                    ReportsScreen(onNavigateBack = { navController.popBackStack() })
                }
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
        NavigationItem.AllFunsion,
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

    object MonthlyReport : NavigationItem(
        route = "monthly_report",
        title = "Monthly Report",
        icon = Icons.Default.PieChart
    )

    object WorkTimer : NavigationItem(
        route = "work_timer",
        title = "Work Timer",
        icon = Icons.Default.Timer
    )

    object Expense : NavigationItem(
        route = "expense",
        title = "Expense Log",
        icon = Icons.Default.AttachMoney
    )
    object Income : NavigationItem(
        route = "income",
        title = "Income",
        icon = Icons.Default.AttachMoney
    )

    object Health : NavigationItem(
        route = "health",
        title = "Health Metrics",
        icon = Icons.Default.Favorite
    )

    object Focus : NavigationItem(
        route = "focus",
        title = "Focus Sessions",
        icon = Icons.Default.FilterCenterFocus
    )

    object Habit : NavigationItem(
        route = "habit",
        title = "Habit Tracker",
        icon = Icons.Default.CheckCircle
    )

    object Achievements : NavigationItem(
        route = "achievements",
        title = "Achievements",
        icon = Icons.Default.EmojiEvents
    )

    object Journal : NavigationItem(
        route = "journal",
        title = "Daily Journal",
        icon = Icons.Default.Book
    )

    object MindfulBreak : NavigationItem(
        route = "mindful_break",
        title = "Mindful Break",
        icon = Icons.Default.SelfImprovement
    )

    object Wisdom : NavigationItem(
        route = "wisdom",
        title = "Wisdom Library",
        icon = Icons.AutoMirrored.Filled.LibraryBooks
    )

    object Calculation : NavigationItem(
        route = "calculation",
        title = "Calculation",
        icon = Icons.Default.Calculate
    )

    object Backup : NavigationItem(
        route = "backup",
        title = "Backup & Restore",
        icon = Icons.Default.Backup
    )

    object AllFunsion : NavigationItem(
        route = "all_funsion",
        title = "All Funsion",
        icon = Icons.Default.AddRoad
    )

    object UserProfile : NavigationItem(
        route = "user_profile",
        title = "User Profile",
        icon = Icons.Default.Person
    )

    object AddEntry : NavigationItem(
        route = "add_entry",
        title = "Add Entry",
        icon = Icons.Default.Add
    )

    object Reports : NavigationItem(
        route = "reports",
        title = "Reports",
        icon = Icons.Default.Assessment
    )
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    SmartWorkTrackerTheme {
        MainApp()
    }
}
