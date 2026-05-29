package com.rudra.smartworktracker.ui.screens.journal

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.DailyJournal
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)
private val CyanLight = Color(0xFF06B6D4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyJournalScreen(viewModel: DailyJournalViewModel = viewModel()) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Journal", "History")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onSurface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(
                        brush = Brush.linearGradient(listOf(VioletPurple, CyanLight)),
                        shape = RoundedCornerShape(10.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Book, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Text("Daily Journal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, title ->
                    CustomTab(
                        title = title,
                        isSelected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (selectedTab) {
                0 -> JournalEditor(
                    viewModel = viewModel,
                    onSave = { editedJournal -> viewModel.saveOrUpdateJournal(editedJournal) }
                )
                1 -> JournalHistory(
                    viewModel = viewModel,
                    onEdit = { journal -> viewModel.updateSelectedDate(journal.date); selectedTab = 0 },
                    onDelete = { journal -> viewModel.deleteJournal(journal) }
                )
            }
        }
    }
}

@Composable
fun CustomTab(title: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(targetValue = if (isSelected) VioletPurple else MaterialTheme.colorScheme.surfaceVariant, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "")
    val contentColor by animateColorAsState(targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, label = "")

    Card(
        modifier = modifier.height(44.dp).clickable(onClick = onClick),
        shape = ChipShape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, color = contentColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun JournalEditor(viewModel: DailyJournalViewModel, onSave: (DailyJournal) -> Unit) {
    val todayJournal by viewModel.todayJournal.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val context = LocalContext.current

    var intention by remember(todayJournal, selectedDate) { mutableStateOf(todayJournal?.morningIntention ?: "") }
    var reflection by remember(todayJournal, selectedDate) { mutableStateOf(todayJournal?.eveningReflection ?: "") }
    var gratitude by remember(todayJournal, selectedDate) { mutableStateOf(todayJournal?.gratitude ?: "") }
    var currentSection by remember { mutableIntStateOf(0) }

    val sections = listOf("Morning Intention", "Evening Reflection", "Gratitude")
    val sectionIcons = listOf(Icons.Default.LightMode, Icons.Default.Nightlight, Icons.Default.Favorite)
    val sectionColors = listOf(SapphireBlue, VioletPurple, CoralRed)

    val autoSaveJob = remember { mutableStateOf<Job?>(null) }
    var autoSaveTrigger by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(intention, reflection, gratitude, autoSaveTrigger) {
        autoSaveJob.value?.cancel()
        if (intention.isNotBlank() || reflection.isNotBlank() || gratitude.isNotBlank()) {
            autoSaveJob.value = coroutineScope.launch {
                kotlinx.coroutines.delay(2000)
                val journalEntry = (todayJournal ?: DailyJournal(date = selectedDate)).copy(morningIntention = intention, eveningReflection = reflection, gratitude = gratitude)
                onSave(journalEntry)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DateHeader(date = selectedDate, onDateChange = { viewModel.updateSelectedDate(it) })
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, ChipShape, clip = false),
                    shape = ChipShape,
                    elevation = CardDefaults.cardElevation(0.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.padding(4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        sections.forEachIndexed { index, section ->
                            val isSelected = currentSection == index
                            val bgColor by animateColorAsState(targetValue = if (isSelected) sectionColors[index].copy(alpha = 0.12f) else Color.Transparent, label = "")
                            val txtColor by animateColorAsState(targetValue = if (isSelected) sectionColors[index] else MaterialTheme.colorScheme.onSurfaceVariant, label = "")

                            Box(
                                modifier = Modifier.weight(1f).clip(ChipShape).background(bgColor).clickable { currentSection = index }.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(sectionIcons[index], null, tint = txtColor, modifier = Modifier.size(18.dp))
                                    Text(section.split(" ")[0], color = txtColor, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            item {
                AnimatedVisibility(visible = currentSection == 0) {
                    JournalSection(title = "Morning Intention", subtitle = "What I want to focus on today",
                        placeholder = "My main goal for today is...\nWhat will make today successful?\nHow do I want to show up today?",
                        value = intention, onValueChange = { intention = it; autoSaveTrigger++ }, icon = Icons.Default.LightMode, color = sectionColors[0], characterLimit = 500)
                }
            }
            item {
                AnimatedVisibility(visible = currentSection == 1) {
                    JournalSection(title = "Evening Reflection", subtitle = "How did today go?",
                        placeholder = "Today went well...\nWhat did I learn today?\nWhat could I improve tomorrow?",
                        value = reflection, onValueChange = { reflection = it; autoSaveTrigger++ }, icon = Icons.Default.Nightlight, color = sectionColors[1], characterLimit = 500)
                }
            }
            item {
                AnimatedVisibility(visible = currentSection == 2) {
                    JournalSection(title = "Gratitude Journal", subtitle = "What I am grateful for today",
                        placeholder = "I am grateful for...\nThe small joys in life\nPeople who supported me\nLessons learned",
                        value = gratitude, onValueChange = { gratitude = it; autoSaveTrigger++ }, icon = Icons.Default.Favorite, color = sectionColors[2], characterLimit = 300)
                }
            }

            item { Spacer(Modifier.height(100.dp)) }
        }

        val isFormEmpty = intention.isBlank() && reflection.isBlank() && gratitude.isBlank()
        val hasChanges = todayJournal?.let { it.morningIntention != intention || it.eveningReflection != reflection || it.gratitude != gratitude } ?: !isFormEmpty
        val buttonScale = remember { Animatable(1f) }

        LaunchedEffect(hasChanges) {
            if (hasChanges) {
                buttonScale.animateTo(1.02f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                buttonScale.animateTo(1f)
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            shape = CardShape,
            tonalElevation = 8.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Button(
                onClick = {
                    val journalEntry = (todayJournal ?: DailyJournal(date = selectedDate)).copy(morningIntention = intention, eveningReflection = reflection, gratitude = gratitude)
                    onSave(journalEntry)
                    Toast.makeText(context, "Journal Saved Successfully!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = ChipShape,
                enabled = hasChanges && !isFormEmpty,
                colors = ButtonDefaults.buttonColors(containerColor = VioletPurple, disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Save, "Save", modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(text = if (todayJournal != null) "Update Journal" else "Save Journal", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun JournalHistory(viewModel: DailyJournalViewModel, onEdit: (DailyJournal) -> Unit, onDelete: (DailyJournal) -> Unit) {
    val journalHistory by viewModel.journalHistory.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<DailyJournal?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredJournals = journalHistory.filter { journal ->
        searchQuery.isEmpty() ||
                journal.morningIntention.contains(searchQuery, ignoreCase = true) ||
                journal.eveningReflection.contains(searchQuery, ignoreCase = true) ||
                journal.gratitude.contains(searchQuery, ignoreCase = true) ||
                journal.date.toDisplayFormat().contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (journalHistory.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search journal entries...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search") },
                shape = ChipShape, singleLine = true
            )
        }

        if (filteredJournals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Default.Book, "No entries", modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text(if (journalHistory.isEmpty()) "No journal entries yet." else "No matching entries found.",
                        style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredJournals, key = { it.id }) { journal ->
                    JournalHistoryItem(journal = journal, onEdit = onEdit, onDelete = { showDeleteDialog = it })
                }
            }
        }
    }

    showDeleteDialog?.let { journal ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            shape = CardShape,
            title = { Text("Delete Journal Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete the journal for ${journal.date.toDisplayFormat()}?") },
            confirmButton = { TextButton(onClick = { onDelete(journal); showDeleteDialog = null }) { Text("Delete", color = CoralRed) } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun JournalHistoryItem(journal: DailyJournal, onEdit: (DailyJournal) -> Unit, onDelete: (DailyJournal) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false).clickable { onEdit(journal) },
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(modifier = Modifier.size(32.dp).background(VioletPurple.copy(alpha = 0.12f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Book, null, tint = VioletPurple, modifier = Modifier.size(16.dp))
                    }
                    Text(journal.date.toDisplayFormat(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onEdit(journal) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { onDelete(journal) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = CoralRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val previewText = buildString {
                if (journal.morningIntention.isNotBlank()) { append("  ${journal.morningIntention.take(60)}"); if (journal.morningIntention.length > 60) append("...") }
                if (journal.eveningReflection.isNotBlank()) { if (isNotEmpty()) append("\n"); append("  ${journal.eveningReflection.take(60)}"); if (journal.eveningReflection.length > 60) append("...") }
                if (journal.gratitude.isNotBlank()) { if (isNotEmpty()) append("\n"); append("  ${journal.gratitude.take(60)}"); if (journal.gratitude.length > 60) append("...") }
            }.ifBlank { "No content" }

            Text(text = previewText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
        }
    }
}

@Composable
fun DateHeader(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            FilledIconButton(
                onClick = { onDateChange(date.minusDays(1)) },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = VioletPurple.copy(alpha = 0.12f), contentColor = VioletPurple)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Day", modifier = Modifier.size(20.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(date.toDisplayFormat(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (date == LocalDate.now()) "Today"
                    else if (date == LocalDate.now().minusDays(1)) "Yesterday"
                    else if (date == LocalDate.now().plusDays(1)) "Tomorrow"
                    else "", style = MaterialTheme.typography.bodySmall, color = VioletPurple, fontWeight = FontWeight.Medium
                )
            }

            FilledIconButton(
                onClick = { onDateChange(date.plusDays(1)) },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = VioletPurple.copy(alpha = 0.12f), contentColor = VioletPurple)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Day", modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun JournalSection(
    title: String, subtitle: String, placeholder: String, value: String, onValueChange: (String) -> Unit,
    icon: ImageVector, color: Color, characterLimit: Int
) {
    var isFocused by remember { mutableStateOf(false) }
    val characterCount = value.length
    val progress = characterCount.toFloat() / characterLimit

    Card(
        modifier = Modifier.fillMaxWidth().shadow(if (isFocused) 8.dp else 4.dp, CardShape, clip = false),
        shape = CardShape,
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isFocused) BorderStroke(2.dp, color) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Icon(icon, title, tint = color, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().clip(ChipShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(16.dp)
            ) {
                BasicTextField(
                    value = value, onValueChange = { if (it.length <= characterLimit) onValueChange(it) },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, lineHeight = 24.sp),
                    decorationBox = { innerTextField ->
                        Box { if (value.isEmpty()) { Text(placeholder, style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp, lineHeight = 24.sp)) }; innerTextField() }
                    }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(6.dp).clip(ChipShape),
                    color = color, trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text("$characterCount/$characterLimit", style = MaterialTheme.typography.bodySmall,
                    color = if (characterCount > characterLimit * 0.9) CoralRed else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    }
}
