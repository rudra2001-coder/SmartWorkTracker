package com.rudra.smartworktracker.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.Achievement
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

private val CardShape = RoundedCornerShape(16.dp)
private val EmeraldGreen = Color(0xFF00C896)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val CoralRed = Color(0xFFFF5757)

val party = Party(
    speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = 360,
    colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
    emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(100),
    position = Position.Relative(0.5, 0.3)
)

@Composable
fun AchievementsScreen(viewModel: AchievementsViewModel = viewModel()) {
    val achievements by viewModel.achievements.collectAsState()
    val newlyUnlocked by viewModel.newlyUnlockedAchievement.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(52.dp).background(
                            brush = Brush.linearGradient(listOf(GoldenAmber, CoralRed)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("Achievements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${achievements.count { it.unlocked }} unlocked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            items(achievements) { achievement ->
                AchievementItem(achievement = achievement)
            }
        }

        if (newlyUnlocked != null) {
            KonfettiView(modifier = Modifier.fillMaxSize(), parties = remember { listOf(party) })
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(if (achievement.unlocked) 6.dp else 2.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).then(
                    if (achievement.unlocked) Modifier.background(Brush.linearGradient(listOf(GoldenAmber, CoralRed)), RoundedCornerShape(12.dp))
                    else Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (achievement.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = if (achievement.unlocked) "Unlocked" else "Locked",
                    tint = if (achievement.unlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(achievement.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(achievement.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (achievement.unlocked) {
                Box(
                    modifier = Modifier.background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Done", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                }
            }
        }
    }
}
