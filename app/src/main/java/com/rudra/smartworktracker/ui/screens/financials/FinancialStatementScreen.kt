package com.rudra.smartworktracker.ui.screens.financials

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.entity.FinancialTransaction
import com.rudra.smartworktracker.data.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialStatementScreen() {
    val context = LocalContext.current
    val viewModel: FinancialStatementViewModel = viewModel(factory = FinancialStatementViewModelFactory(context.applicationContext as Application))
    val transactions by viewModel.transactions.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netFlow by viewModel.netFlow.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Financial Statement") })
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            item {
                SummaryHeader(totalIncome, totalExpenses, netFlow)
            }
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }
}

@Composable
fun SummaryHeader(totalIncome: Double, totalExpenses: Double, netFlow: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Account Summary", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                SummaryItem("Income", totalIncome, Color.Green)
                SummaryItem("Expenses", totalExpenses, Color.Red)
                SummaryItem("Net Flow", netFlow, if (netFlow >= 0) Color.Green else Color.Red)
            }
        }
    }
}

@Composable
fun SummaryItem(title: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(String.format("%.2f", amount), style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TransactionItem(transaction: FinancialTransaction) {
    val formattedDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(transaction.date))
    val icon = when (transaction.type) {
        TransactionType.INCOME, TransactionType.LOAN_RECEIVE -> Icons.Default.ArrowUpward
        TransactionType.EXPENSE, TransactionType.EMI_PAID -> Icons.Default.ArrowDownward
        else -> Icons.AutoMirrored.Filled.CompareArrows
    }
    val color = when (transaction.type) {
        TransactionType.INCOME, TransactionType.LOAN_RECEIVE -> Color.Green
        TransactionType.EXPENSE, TransactionType.EMI_PAID -> Color.Red
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.type.name,
                tint = color,
                modifier = Modifier.size(24.dp).clip(CircleShape).background(color.copy(alpha = 0.1f))
            )
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
                Text(text = transaction.note.ifEmpty { transaction.type.name.replace("_", " ") }, style = MaterialTheme.typography.titleMedium)
                Text(text = formattedDate, style = MaterialTheme.typography.bodySmall)
            }
            Text(text = String.format("%.2f", transaction.amount), style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}
