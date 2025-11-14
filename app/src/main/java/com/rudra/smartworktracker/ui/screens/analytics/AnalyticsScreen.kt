package com.rudra.smartworktracker.ui.screens.analytics

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalyticsScreen(onNavigateBack: () -> Unit) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: AnalyticsViewModel = viewModel(factory = AnalyticsViewModelFactory(application))
    val analyticsData by viewModel.analyticsData.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            FinancialSummaryChart(incomes = analyticsData.incomes, expenses = analyticsData.expenses)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            ProductivityScoreCard(score = analyticsData.productivityScore)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            FocusQualityChart(focusSessions = analyticsData.focusSessions)
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            HabitConsistencyCard(habits = analyticsData.habits)
        }
    }
}

@Composable
fun FinancialSummaryChart(incomes: List<com.rudra.smartworktracker.data.entity.Income>, expenses: List<com.rudra.smartworktracker.model.Expense>) {
    val totalIncome = incomes.sumOf { it.amount }
    val totalExpense = expenses.sumOf { it.amount }
    val savings = totalIncome - totalExpense

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Financial Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income")
                    Text("%.2f".format(totalIncome), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense")
                    Text("%.2f".format(totalExpense), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Savings")
                    Text("%.2f".format(savings), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = if (savings >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ProductivityScoreCard(score: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Productivity Score", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$score / 100", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = score / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}

@Composable
fun FocusQualityChart(focusSessions: List<com.rudra.smartworktracker.model.FocusSession>) {
    val totalDeepWork = focusSessions.filter { it.type == com.rudra.smartworktracker.model.FocusType.DEEP_WORK }.sumOf { it.duration } / 60
    val totalPomodoro = focusSessions.filter { it.type == com.rudra.smartworktracker.model.FocusType.POMODORO }.sumOf { it.duration } / 60

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Focus Quality (in minutes)", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Deep Work")
                    Text(totalDeepWork.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Pomodoro")
                    Text(totalPomodoro.toString(), fontSize = 30.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HabitConsistencyCard(habits: List<com.rudra.smartworktracker.model.Habit>) {
    val totalHabits = habits.size
    val completedHabits = habits.count { it.streak > 0 }
    val consistency = if (totalHabits > 0) (completedHabits.toFloat() / totalHabits.toFloat()) * 100 else 0f

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Habit Consistency", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${consistency.toInt()}%", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = consistency / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
        }
    }
}
