package com.rudra.smartworktracker.ui.screens.journal

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.DailyJournal
import java.time.LocalDate

@Composable
fun DailyJournalScreen(viewModel: DailyJournalViewModel = viewModel()) {
    val todayJournal by viewModel.todayJournal.collectAsState()
    val context = LocalContext.current

    var intention by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }
    var gratitude by remember { mutableStateOf("") }

    LaunchedEffect(todayJournal) {
        todayJournal?.let {
            intention = it.morningIntention
            reflection = it.eveningReflection
            gratitude = it.gratitude
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Daily Journal", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Morning Intention
        Text("What I want to focus on today", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = intention,
            onValueChange = { intention = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("My main goal for today is...") }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Evening Reflection
        Text("How did today go?", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = reflection,
            onValueChange = { reflection = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Today went well... I learned that...") }
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Gratitude Journaling
        Text("What I am grateful for today", style = MaterialTheme.typography.titleMedium)
        TextField(
            value = gratitude,
            onValueChange = { gratitude = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("I am grateful for...") }
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val journalEntry = DailyJournal(
                    date = todayJournal?.date ?: LocalDate.now(),
                    morningIntention = intention,
                    eveningReflection = reflection,
                    gratitude = gratitude
                )
                viewModel.saveJournal(journalEntry)
                Toast.makeText(context, "Journal Saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Journal")
        }
    }
}
