package com.rudra.smartworktracker.ui.screens.journal

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.DailyJournal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyJournalScreen(viewModel: DailyJournalViewModel = viewModel()) {
    val todayJournal by viewModel.todayJournal.collectAsState(initial = null)
    val context = LocalContext.current

    var intention by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var gratitude by remember { mutableStateOf("") }
    var currentSection by remember { mutableStateOf(0) }

    LaunchedEffect(todayJournal) {
        todayJournal?.let {
            intention = it.morningIntention
            reflection = it.eveningReflection
            gratitude = it.gratitude
        }
    }

    val sections = listOf("Morning Intention", "Evening Reflection", "Gratitude")
    val sectionIcons = listOf(Icons.Default.LightMode, Icons.Default.Nightlight, Icons.Default.Favorite)
    val sectionColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        // Header with date
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    clip = true
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Daily Journal",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Section Navigation
        PrimaryTabRow(
            selectedTabIndex = currentSection,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = {}
        ) {
            sections.forEachIndexed { index, section ->
                val isSelected = currentSection == index
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) sectionColors[index] else Color.Transparent,
                    label = "tab color"
                )

                Tab(
                    selected = isSelected,
                    onClick = { currentSection = index },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(backgroundColor)
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            sectionIcons[index],
                            contentDescription = section,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            section,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AnimatedVisibility(visible = currentSection == 0) {
                    JournalSection(
                        title = "Morning Intention",
                        subtitle = "What I want to focus on today",
                        placeholder = "My main goal for today is...\n\nI will achieve this by...\n\nMy energy will be focused on...",
                        value = intention,
                        onValueChange = { intention = it },
                        icon = Icons.Default.LightMode,
                        color = sectionColors[0],
                        characterLimit = 500
                    )
                }
            }

            item {
                AnimatedVisibility(visible = currentSection == 1) {
                    JournalSection(
                        title = "Evening Reflection",
                        subtitle = "How did today go?",
                        placeholder = "Today went well...\n\nI learned that...\n\nWhat could I improve...",
                        value = reflection,
                        onValueChange = { reflection = it },
                        icon = Icons.Default.Nightlight,
                        color = sectionColors[1],
                        characterLimit = 500
                    )
                }
            }

            item {
                AnimatedVisibility(visible = currentSection == 2) {
                    JournalSection(
                        title = "Gratitude Journal",
                        subtitle = "What I am grateful for today",
                        placeholder = "I am grateful for...\n\nI appreciate...\n\nToday's blessings...",
                        value = gratitude,
                        onValueChange = { gratitude = it },
                        icon = Icons.Default.Favorite,
                        color = sectionColors[2],
                        characterLimit = 300
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Save Button - Sticky at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val isFormEmpty = intention.isBlank() && reflection.isBlank() && gratitude.isBlank()
            val buttonScale = remember { Animatable(1f) }

            LaunchedEffect(isFormEmpty) {
                if (!isFormEmpty) {
                    buttonScale.animateTo(
                        1.05f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    buttonScale.animateTo(1f)
                }
            }

            Button(
                onClick = {
                    val journalEntry = DailyJournal(
                        date = todayJournal?.date ?: LocalDate.now(),
                        morningIntention = intention,
                        eveningReflection = reflection,
                        gratitude = gratitude
                    )
                    viewModel.saveJournal(journalEntry)
                    Toast.makeText(context, "✨ Journal Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(buttonScale.value)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isFormEmpty
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Save Journal Entry",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun JournalSection(
    title: String,
    subtitle: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    characterLimit: Int
) {
    var isFocused by remember { mutableStateOf(false) }
    val characterCount = value.length
    val progress = characterCount.toFloat() / characterLimit

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 12.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = true
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = if (isFocused) BorderStroke(2.dp, color) else null
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= characterLimit) onValueChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                placeholder,
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Character Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    "$characterCount/$characterLimit",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (characterCount > characterLimit * 0.9) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Preview helper for development
@Composable
fun JournalPreview() {
    MaterialTheme {
        DailyJournalScreen()
    }
}
