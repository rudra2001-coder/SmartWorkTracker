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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.Goal
import com.rudra.smartworktracker.model.GoalCategory
import com.rudra.smartworktracker.model.Target
import com.rudra.smartworktracker.model.Wisdom
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun WisdomScreen(viewModel: WisdomViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Wisdom", "Life Plan", "Boosters", "Shop")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StatsDashboard(viewModel)

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Level ${stats.level}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stats.experiencePoints} XP to next level",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = stats.level.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val xpForNextLevel = stats.level * 500
            val progress = stats.experiencePoints.toFloat() / xpForNextLevel.toFloat()
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Whatshot, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.streak} Day Streak",
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Shield, contentDescription = null, tint = Color.Blue, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${stats.streakProtectionAvailable} Protections",
                    style = MaterialTheme.typography.labelMedium
                )
                if (stats.xpMultiplier > 1.0f) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${stats.xpMultiplier}x XP",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF9800),
                        fontWeight = FontWeight.Bold
                    )
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
            Text("No wisdom available yet.", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        val groupedWisdom = remember { wisdomList.groupBy { it.category } }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedWisdom.forEach { (category, wisdoms) ->
                item {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
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
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\"${wisdom.text}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic
            )
            wisdom.author?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- $it",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
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
            onAddGoal = {
                viewModel.addGoal(it)
                showAddGoalDialog = false
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Life Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { showAddGoalDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
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
        title = { Text("Add a New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    TextField(
                        value = selectedCategory.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GoalCategory.entries.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = {
                                selectedCategory = category
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val goal = Goal(
                        title = title,
                        description = description,
                        category = selectedCategory
                    )
                    onAddGoal(goal)
                },
                enabled = title.isNotBlank() && description.isNotBlank()
            ) {
                Text("Add Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = MaterialTheme.colorScheme.error)
                }

                if (goal.isCompleted) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = Color(0xFF4CAF50))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = goal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val progress = if (goal.totalTargets > 0) {
                goal.completedTargets.toFloat() / goal.totalTargets.toFloat()
            } else 0f
            
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "GoalProgress"
            )
            
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (goal.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${goal.completedTargets}/${goal.totalTargets} targets completed",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun GoalDetailSection(goal: Goal, viewModel: WisdomViewModel) {
    val targets = viewModel.targetsMap[goal.id] ?: emptyList()
    var newTargetTitle by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Targets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            targets.forEach { target ->
                TargetItem(
                    target = target,
                    onComplete = { viewModel.completeTarget(goal.id, target.id) },
                    onDelete = { viewModel.deleteTarget(goal.id, target.id) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTargetTitle,
                    onValueChange = { newTargetTitle = it },
                    label = { Text("New Target") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (newTargetTitle.isNotBlank()) {
                            viewModel.addTargetToGoal(goal.id, newTargetTitle)
                            newTargetTitle = ""
                        }
                    },
                    enabled = newTargetTitle.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Target")
                }
            }
        }
    }
}

@Composable
fun TargetItem(target: Target, onComplete: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = target.isCompleted,
            onCheckedChange = { if (it) onComplete() },
            enabled = !target.isCompleted
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = target.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (target.isCompleted) FontWeight.Normal else FontWeight.Medium,
            color = if (target.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (target.isCompleted) TextDecoration.LineThrough else null,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Close, contentDescription = "Delete Target", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun GoalCelebrationAnimation(achievementName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "goal_celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(onClick = {}),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color.Yellow,
                modifier = Modifier.size(150.dp).graphicsLayer(scaleX = scale, scaleY = scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "GOAL MASTERED!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "🎯 $achievementName 🎯",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Cyan,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🎉 +200 XP Bonus! 🎉",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun TargetCelebrationAnimation(achievementName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Target Achieved!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = achievementName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "+50 XP",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EnhancedShopScreen(viewModel: WisdomViewModel) {
    val stats = viewModel.userStats
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Streak Protection", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProtectionOption(name = "1 Prot.", cost = 100, xpBalance = stats.experiencePoints) {
                    viewModel.purchaseStreakProtection(1, 100)
                }
                ProtectionOption(name = "3 Prot.", cost = 250, xpBalance = stats.experiencePoints) {
                    viewModel.purchaseStreakProtection(3, 250)
                }
                ProtectionOption(name = "5 Prot.", cost = 400, xpBalance = stats.experiencePoints) {
                    viewModel.purchaseStreakProtection(5, 400)
                }
            }
        }
        
        item {
            Text("XP Boosters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BoosterOption(name = "1.5x XP", cost = 100, xpBalance = stats.experiencePoints) {
                    viewModel.purchaseBooster(BoosterType.XP_1_5X, 100)
                }
                BoosterOption(name = "2.0x XP", cost = 150, xpBalance = stats.experiencePoints) {
                    viewModel.purchaseBooster(BoosterType.XP_2X, 150)
                }
            }
        }
    }
}

@Composable
fun RowScope.ProtectionOption(name: String, cost: Int, xpBalance: Int, onPurchase: () -> Unit) {
    val canAfford = xpBalance >= cost
    Card(
        onClick = onPurchase,
        enabled = canAfford,
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = if (canAfford) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = if (canAfford) MaterialTheme.colorScheme.primary else Color.Gray)
            Text(name, style = MaterialTheme.typography.labelMedium)
            Text("$cost XP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RowScope.BoosterOption(name: String, cost: Int, xpBalance: Int, onPurchase: () -> Unit) {
    val canAfford = xpBalance >= cost
    Card(
        onClick = onPurchase,
        enabled = canAfford,
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = if (canAfford) Color(0xFFFFF9C4) else MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = if (canAfford) Color(0xFFFF9800) else Color.Gray)
            Text(name, style = MaterialTheme.typography.labelMedium)
            Text("$cost XP", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BoosterManagementScreen(viewModel: WisdomViewModel) {
    val inventory = viewModel.inventoryBoosters
    val active = viewModel.activeBoosters
    
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Active Boosters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (active.isEmpty()) {
            item { Text("No boosters active.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(active) { booster ->
                ActiveBoosterItem(booster)
            }
        }
        
        item {
            Text("Inventory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        if (inventory.isEmpty()) {
            item { Text("Your inventory is empty.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
        } else {
            items(inventory) { booster ->
                InventoryBoosterItem(booster) { viewModel.activateBooster(booster) }
            }
        }
    }
}

@Composable
fun ActiveBoosterItem(booster: Booster) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFF9800))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(booster.type.name.replace("_", " "), fontWeight = FontWeight.Bold)
                val timeLeft = remember(booster.activatedAt) {
                    val end = booster.activatedAt?.plusHours(booster.durationHours.toLong())
                    val now = LocalDateTime.now()
                    if (end != null && end.isAfter(now)) {
                        val diff = Duration.between(now, end)
                        String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
                    } else "00:00"
                }
                Text("Time left: $timeLeft", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun InventoryBoosterItem(booster: Booster, onActivate: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(booster.type.name.replace("_", " "), fontWeight = FontWeight.Bold)
                    Text("${booster.durationHours} Hour Duration", style = MaterialTheme.typography.labelSmall)
                }
            }
            Button(onClick = onActivate) {
                Text("Activate")
            }
        }
    }
}
