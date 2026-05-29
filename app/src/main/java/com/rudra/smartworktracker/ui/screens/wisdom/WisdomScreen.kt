package com.rudra.smartworktracker.ui.screens.wisdom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.Goal
import com.rudra.smartworktracker.model.GoalCategory
import com.rudra.smartworktracker.model.Target
import com.rudra.smartworktracker.model.Wisdom
import java.time.Duration
import java.time.LocalDateTime

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val CyanLight = Color(0xFF06B6D4)
private val PinkAccent = Color(0xFFEC4899)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WisdomScreen(viewModel: WisdomViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wisdom", "Life Plan", "Boosters", "Shop")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StatsDashboard(viewModel)

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = VioletPurple
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = VioletPurple,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> WisdomContent(viewModel)
                    1 -> GoalScreen(viewModel)
                    2 -> BoosterManagementScreen(viewModel)
                    3 -> EnhancedShopScreen(viewModel)
                }
            }
        }

        if (viewModel.showGoalCelebration) {
            GoalCelebrationAnimation(achievementName = viewModel.lastAchievedItemName)
        }

        AnimatedVisibility(
            visible = viewModel.showTargetCelebration,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TargetCelebrationAnimation(achievementName = viewModel.lastAchievedItemName)
        }
    }
}

@Composable
fun StatsDashboard(viewModel: WisdomViewModel) {
    val stats = viewModel.userStats

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(6.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).background(
                            brush = Brush.linearGradient(listOf(VioletPurple, PinkAccent)),
                            shape = RoundedCornerShape(12.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text("Level ${stats.level}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${stats.experiencePoints} XP to next level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Box(
                    modifier = Modifier.size(44.dp).background(
                        brush = Brush.linearGradient(listOf(VioletPurple, PinkAccent)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stats.level.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val xpForNextLevel = stats.level * 500
            val progress = stats.experiencePoints.toFloat() / xpForNextLevel.toFloat()

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = VioletPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Whatshot, null, tint = CoralRed, modifier = Modifier.size(16.dp))
                    Text("${stats.streak} Day Streak", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Shield, null, tint = SapphireBlue, modifier = Modifier.size(16.dp))
                    Text("${stats.streakProtectionAvailable} Protections", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (stats.xpMultiplier > 1.0f) {
                    Spacer(Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Bolt, null, tint = GoldenAmber, modifier = Modifier.size(16.dp))
                        Text("${stats.xpMultiplier}x XP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = GoldenAmber)
                    }
                }
            }
        }
    }
}

@Composable
fun WisdomContent(viewModel: WisdomViewModel) {
    val wisdomList = remember { viewModel.getWisdom() }

    if (wisdomList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("No wisdom available yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        val groupedWisdom = remember { wisdomList.groupBy { it.category } }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            groupedWisdom.forEach { (category, wisdoms) ->
                item {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).background(
                                brush = Brush.linearGradient(listOf(VioletPurple, PinkAccent)),
                                shape = RoundedCornerShape(8.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                items(wisdoms) { wisdom ->
                    WisdomItem(wisdom = wisdom)
                }
            }
        }
    }
}

@Composable
fun WisdomItem(wisdom: Wisdom) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(8.dp).padding(top = 6.dp).background(VioletPurple, CircleShape)
            )
            Column {
                Text(text = "\"${wisdom.text}\"", style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface)
                wisdom.author?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "- $it", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun GoalScreen(viewModel: WisdomViewModel) {
    val goals = viewModel.goals
    val selectedGoal = viewModel.selectedGoal
    var showAddGoalDialog by remember { mutableStateOf(false) }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAddGoal = { viewModel.addGoal(it); showAddGoalDialog = false }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(32.dp).background(
                            brush = Brush.linearGradient(listOf(EmeraldGreen, SapphireBlue)),
                            shape = RoundedCornerShape(8.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Flag, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Text("Life Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                FilledTonalIconButton(onClick = { showAddGoalDialog = true }, colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = EmeraldGreen.copy(alpha = 0.15f), contentColor = EmeraldGreen)) {
                    Icon(Icons.Default.Add, "Add Goal")
                }
            }
        }

        items(goals) { goal ->
            GoalCard(
                goal = goal,
                onClick = { viewModel.selectGoal(if (selectedGoal?.id == goal.id) null else goal) },
                onDelete = { viewModel.deleteGoal(goal.id) }
            )

            if (selectedGoal?.id == goal.id) {
                GoalDetailSection(goal = goal, viewModel = viewModel)
            }
        }

        if (goals.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Flag, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        Spacer(Modifier.height(12.dp))
                        Text("No goals yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to start planning", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onAddGoal: (Goal) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(GoalCategory.PERSONAL) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = CardShape,
        title = { Text("Add a New Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Goal Title") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = ChipShape)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = ChipShape)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedCategory.name, onValueChange = {}, readOnly = true, label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = ChipShape
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GoalCategory.entries.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAddGoal(Goal(title = title, description = description, category = selectedCategory)) }, enabled = title.isNotBlank() && description.isNotBlank()) {
                Text("Add Goal")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = if (goal.isCompleted) EmeraldGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(36.dp).background(
                        brush = Brush.linearGradient(
                            when (goal.category) {
                                GoalCategory.PERSONAL -> listOf(EmeraldGreen, SapphireBlue)
                                GoalCategory.CAREER -> listOf(SapphireBlue, VioletPurple)
                                GoalCategory.HEALTH -> listOf(EmeraldGreen, GoldenAmber)
                                GoalCategory.FINANCE -> listOf(GoldenAmber, CoralRed)
                                GoalCategory.LEARNING -> listOf(VioletPurple, PinkAccent)
                                GoalCategory.OTHER -> listOf(CoralRed, CyanLight)
                            }
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            when (goal.category) {
                                GoalCategory.PERSONAL -> Icons.Default.Person
                                GoalCategory.CAREER -> Icons.Default.Work
                                GoalCategory.HEALTH -> Icons.Default.Favorite
                                GoalCategory.FINANCE -> Icons.Default.AttachMoney
                                GoalCategory.LEARNING -> Icons.Default.School
                                GoalCategory.OTHER -> Icons.Default.MoreHoriz
                            }, null, tint = Color.White, modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                }
                if (goal.isCompleted) {
                    Icon(Icons.Default.CheckCircle, "Completed", tint = EmeraldGreen)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "Delete Goal", tint = CoralRed.copy(alpha = 0.6f))
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(goal.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(12.dp))

            val progress = if (goal.totalTargets > 0) goal.completedTargets.toFloat() / goal.totalTargets.toFloat() else 0f
            val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000, easing = FastOutSlowInEasing), label = "GoalProgress")

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = if (goal.isCompleted) EmeraldGreen else VioletPurple,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(6.dp))
            Text("${goal.completedTargets}/${goal.totalTargets} targets completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = CardShape,
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal?") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteConfirm = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun GoalDetailSection(goal: Goal, viewModel: WisdomViewModel) {
    val targets = viewModel.targetsMap[goal.id] ?: emptyList()
    var newTargetTitle by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(VioletPurple.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Checklist, null, tint = VioletPurple, modifier = Modifier.size(14.dp))
                }
                Text("Targets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(Modifier.height(12.dp))

            if (targets.isEmpty()) {
                Text("No targets yet. Add one below.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                targets.forEach { target ->
                    TargetItem(
                        target = target,
                        onComplete = { viewModel.completeTarget(goal.id, target.id) },
                        onDelete = { viewModel.deleteTarget(goal.id, target.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTargetTitle,
                    onValueChange = { newTargetTitle = it },
                    label = { Text("New Target") },
                    modifier = Modifier.weight(1f),
                    shape = ChipShape,
                    singleLine = true
                )
                FilledIconButton(
                    onClick = {
                        if (newTargetTitle.isNotBlank()) {
                            viewModel.addTargetToGoal(goal.id, newTargetTitle)
                            newTargetTitle = ""
                        }
                    },
                    enabled = newTargetTitle.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = VioletPurple, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Add, "Add Target")
                }
            }
        }
    }
}

@Composable
fun TargetItem(target: Target, onComplete: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = target.isCompleted,
            onCheckedChange = { if (it) onComplete() },
            enabled = !target.isCompleted,
            colors = CheckboxDefaults.colors(checkedColor = EmeraldGreen)
        )

        Text(
            text = target.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (target.isCompleted) FontWeight.Normal else FontWeight.Medium,
            color = if (target.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (target.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, "Delete Target", tint = CoralRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GoalCelebrationAnimation(achievementName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "goal_celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(animation = tween(500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Star, null, tint = Color.Yellow, modifier = Modifier.size(150.dp).graphicsLayer(scaleX = scale, scaleY = scale))
            Spacer(Modifier.height(24.dp))
            Text("GOAL MASTERED!", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text("  $achievementName  ", style = MaterialTheme.typography.headlineSmall, color = CyanLight, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            Text("  +200 XP Bonus!  ", style = MaterialTheme.typography.titleLarge, color = Color.White)
        }
    }
}

@Composable
fun TargetCelebrationAnimation(achievementName: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp).shadow(8.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = EmeraldGreen, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Target Achieved!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(achievementName, style = MaterialTheme.typography.bodySmall, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier.background(GoldenAmber.copy(alpha = 0.15f), PillShape).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("+50 XP", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = GoldenAmber)
            }
        }
    }
}

@Composable
fun EnhancedShopScreen(viewModel: WisdomViewModel) {
    val stats = viewModel.userStats

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(
                        brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                        shape = RoundedCornerShape(8.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Shop", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(24.dp).background(CoralRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Shield, null, tint = CoralRed, modifier = Modifier.size(14.dp))
                        }
                        Text("Streak Protection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProtectionOption(name = "1 Prot.", cost = 100, xpBalance = stats.experiencePoints) { viewModel.purchaseStreakProtection(1, 100) }
                        ProtectionOption(name = "3 Prot.", cost = 250, xpBalance = stats.experiencePoints) { viewModel.purchaseStreakProtection(3, 250) }
                        ProtectionOption(name = "5 Prot.", cost = 400, xpBalance = stats.experiencePoints) { viewModel.purchaseStreakProtection(5, 400) }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(24.dp).background(GoldenAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bolt, null, tint = GoldenAmber, modifier = Modifier.size(14.dp))
                        }
                        Text("XP Boosters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoosterOption(name = "1.5x XP", cost = 100, xpBalance = stats.experiencePoints) { viewModel.purchaseBooster(BoosterType.XP_1_5X, 100) }
                        BoosterOption(name = "2.0x XP", cost = 150, xpBalance = stats.experiencePoints) { viewModel.purchaseBooster(BoosterType.XP_2X, 150) }
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ProtectionOption(name: String, cost: Int, xpBalance: Int, onPurchase: () -> Unit) {
    val canAfford = xpBalance >= cost
    Card(
        onClick = onPurchase, enabled = canAfford,
        modifier = Modifier.weight(1f).shadow(if (canAfford) 4.dp else 0.dp, RoundedCornerShape(14.dp), clip = false),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = if (canAfford) CoralRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Shield, null, tint = if (canAfford) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("$cost XP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (canAfford) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RowScope.BoosterOption(name: String, cost: Int, xpBalance: Int, onPurchase: () -> Unit) {
    val canAfford = xpBalance >= cost
    Card(
        onClick = onPurchase, enabled = canAfford,
        modifier = Modifier.weight(1f).shadow(if (canAfford) 4.dp else 0.dp, RoundedCornerShape(14.dp), clip = false),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = if (canAfford) GoldenAmber.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Bolt, null, tint = if (canAfford) GoldenAmber else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("$cost XP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (canAfford) GoldenAmber else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BoosterManagementScreen(viewModel: WisdomViewModel) {
    val inventory = viewModel.inventoryBoosters
    val active = viewModel.activeBoosters

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).background(
                        brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                        shape = RoundedCornerShape(8.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Text("Boosters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayCircle, null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                }
                Text("Active Boosters", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (active.isEmpty()) {
            item {
                Text("No boosters active.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
        } else {
            items(active) { booster -> ActiveBoosterItem(booster) }
        }

        item { Spacer(Modifier.height(4.dp)) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(24.dp).background(SapphireBlue.copy(alpha = 0.15f), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Inventory, null, tint = SapphireBlue, modifier = Modifier.size(14.dp))
                }
                Text("Inventory", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        if (inventory.isEmpty()) {
            item {
                Text("Your inventory is empty.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
        } else {
            items(inventory) { booster -> InventoryBoosterItem(booster) { viewModel.activateBooster(booster) } }
        }
    }
}

@Composable
fun ActiveBoosterItem(booster: Booster) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(40.dp).background(GoldenAmber.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, null, tint = GoldenAmber)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(booster.type.name.replace("_", " "), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                val timeLeft = remember(booster.activatedAt) {
                    val end = booster.activatedAt?.plusHours(booster.durationHours.toLong())
                    val now = LocalDateTime.now()
                    if (end != null && end.isAfter(now)) {
                        val diff = Duration.between(now, end)
                        String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
                    } else "00:00"
                }
                Text("Time left: $timeLeft", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box(
                modifier = Modifier.background(EmeraldGreen.copy(alpha = 0.15f), PillShape).padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Active", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
            }
        }
    }
}

@Composable
fun InventoryBoosterItem(booster: Booster, onActivate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier.size(40.dp).background(SapphireBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Bolt, null, tint = SapphireBlue)
                }
                Column {
                    Text(booster.type.name.replace("_", " "), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("${booster.durationHours} Hour Duration", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = onActivate,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = Color.White),
                shape = PillShape
            ) {
                Text("Activate")
            }
        }
    }
}
