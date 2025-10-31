package com.rudra.smartworktracker.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rudra.smartworktracker.di.DatabaseModule
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(DatabaseModule.provideDatabase(context), context)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Profile Section
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically() + fadeIn()
                ) {
                    ProfileSection(
                        userName = "Rudra",
                        userEmail = "rudra@mahmudulhasanrudra.com",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            // App Settings
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 100)) + fadeIn()
                ) {
                    SettingsCategory(title = "App Settings")
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 150)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifications",
                        subtitle = "Manage work reminders",
                        isEnabled = uiState.notificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications,
                        showSwitch = true
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 200)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Switch between light and dark mode",
                        isEnabled = uiState.darkThemeEnabled,
                        onCheckedChange = viewModel::toggleDarkTheme,
                        showSwitch = true
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 250)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Vibration,
                        title = "Vibration",
                        subtitle = "Haptic feedback for interactions",
                        isEnabled = uiState.vibrationEnabled,
                        onCheckedChange = viewModel::toggleVibration,
                        showSwitch = true
                    )
                }
            }

            // Data Management
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 300)) + fadeIn()
                ) {
                    SettingsCategory(title = "Data Management")
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 350)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Backup,
                        title = "Backup Data",
                        subtitle = "Export your work history",
                        onClick = { viewModel.backupData() }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 400)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Restore,
                        title = "Restore Data",
                        subtitle = "Import previous backups",
                        onClick = { viewModel.restoreData() }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 450)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Delete,
                        title = "Clear All Data",
                        subtitle = "Reset the app to default",
                        onClick = { viewModel.showClearDataDialog() }
                    )
                }
            }

            // Support
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 500)) + fadeIn()
                ) {
                    SettingsCategory(title = "Support")
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 550)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.AutoMirrored.Filled.Help,
                        title = "Help & Support",
                        subtitle = "Get help using the app",
                        onClick = { viewModel.openHelp() }
                    )
                }
            }

            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 600)) + fadeIn()
                ) {
                    SettingItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        subtitle = "App version and information",
                        onClick = { viewModel.openAbout() }
                    )
                }
            }

            // App Version
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(animationSpec = tween(300, delayMillis = 700)) + fadeIn()
                ) {
                    AppVersionSection(
                        version = "1.0.0",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    // Clear Data Confirmation Dialog
    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideClearDataDialog,
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all your work history? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        viewModel.hideClearDataDialog()
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideClearDataDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    val context = LocalContext.current
    val mockViewModel = SettingsViewModel.factory(DatabaseModule.provideDatabase(context), context)
    SettingsScreen(
        onNavigateBack = {},
        navController = null
    )
}

open class SettingsViewModelFake : ViewModel() {
    open val uiState = MutableStateFlow(SettingsUiState())
    fun toggleNotifications(enabled: Boolean) {}
    fun toggleDarkTheme(enabled: Boolean) {}
    fun toggleVibration(enabled: Boolean) {}
    fun backupData() {}
    fun restoreData() {}
    fun showClearDataDialog() {}
    fun hideClearDataDialog() {}
    fun clearAllData() {}
    fun openHelp() {}
    fun openAbout() {}
}

@Composable
fun ProfileSection(
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar with animation
            ProfileAvatar(
                userName = userName,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    userName: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "profile_avatar")
    val borderScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "border_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Animated border
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = CircleShape
                )
                .graphicsLayer {
                    scaleX = borderScale
                    scaleY = borderScale
                }
        )

        // Avatar content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showSwitch: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "setting_item_bg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            ),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showSwitch) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AppVersionSection(
    version: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        Text(
            text = "Smart Work Tracker",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Version $version",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Made with ❤️ for productive work days",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
