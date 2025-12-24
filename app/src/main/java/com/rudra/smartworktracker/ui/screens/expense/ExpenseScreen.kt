package com.rudra.smartworktracker.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.ExpenseCategory
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextAlign
import android.widget.Toast
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(viewModel: ExpenseViewModel = viewModel()) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.MEAL) }
    val context = LocalContext.current

    // Premium gradient colors
    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF6B6B), // Coral red
            Color(0xFFFF8E53)  // Orange
        )
    )

    val secondaryGradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF36D1DC), // Cyan
            Color(0xFF5B86E5)  // Light blue
        )
    )

    val categoryGradients = mapOf(
        ExpenseCategory.MEAL to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))
        ),
        ExpenseCategory.TRANSPORT to Brush.horizontalGradient(
            colors = listOf(Color(0xFFA1C4FD), Color(0xFFC2E9FB))
        ),
        ExpenseCategory.SHOPPING to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD1FF), Color(0xFFF9FFA4))
        ),
        ExpenseCategory.ENTERTAINMENT to Brush.horizontalGradient(
            colors = listOf(Color(0xFFFBC2EB), Color(0xFFA6C1EE))
        ),
        ExpenseCategory.BILLS to Brush.horizontalGradient(
            colors = listOf(Color(0xFF43CBFF), Color(0xFF9708CC))
        ),

        ExpenseCategory.OTHER to Brush.horizontalGradient(
            colors = listOf(Color(0xFFD4D4D4), Color(0xFFA0A0A0))
        )
    )

    // Custom text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color(0xFFFF6B6B),
        unfocusedBorderColor = Color(0xFFE2E8F0),
        focusedLabelColor = Color(0xFFFF6B6B),
        unfocusedLabelColor = Color(0xFF718096),
        focusedTextColor = Color(0xFF2D3748),
        unfocusedTextColor = Color(0xFF4A5568),
        cursorColor = Color(0xFFFF6B6B),
        errorBorderColor = Color(0xFFE53E3E),
        errorLabelColor = Color(0xFFE53E3E),
        errorSupportingTextColor = Color(0xFFE53E3E)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF5F5), // Very light red tint
                        Color(0xFFFFEBEB)  // Slightly deeper tint
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Log Your Expense",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D3748)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Expense Input Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = true
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Amount Field
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Enter amount") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MoneyOff,
                                contentDescription = "Amount",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    // Categories Section
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ExpenseCategory.values()) { category ->
                            val isSelected = selectedCategory == category
                            val gradient = categoryGradients[category] ?: Brush.horizontalGradient(
                                listOf(Color(0xFFD4D4D4), Color(0xFFA0A0A0))
                            )

                            Box(
                                modifier = Modifier
                                    .shadow(
                                        elevation = if (isSelected) 8.dp else 4.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        brush = if (isSelected) gradient else Brush.linearGradient(
                                            colors = listOf(Color.White, Color.White)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFFF6B6B) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedCategory = category }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Category icon based on type
                                    val icon = when (category) {
                                        ExpenseCategory.MEAL -> Icons.Default.ShoppingCart
                                        ExpenseCategory.TRANSPORT -> androidx.compose.material.icons.Icons.Default.DirectionsCar
                                        ExpenseCategory.SHOPPING -> Icons.Default.Store
                                        ExpenseCategory.ENTERTAINMENT -> androidx.compose.material.icons.Icons.Default.Movie
                                        ExpenseCategory.BILLS -> androidx.compose.material.icons.Icons.Default.Receipt
                                        ExpenseCategory.OTHER -> Icons.Default.List
                                    }

                                    Icon(
                                        imageVector = icon,
                                        contentDescription = category.name,
                                        tint = if (isSelected) Color.White else Color(0xFF4A5568),
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Text(
                                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF4A5568)
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Notes Field
                    Text(
                        text = "Notes (Optional)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A5568)
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Add any notes") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = "Notes",
                                tint = Color(0xFFFF6B6B)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = textFieldColors,
                        maxLines = 4
                    )
                }
            }

            // Save Button
            Button(
                onClick = {
                    if (amount.isNotBlank()) {
                        viewModel.saveExpense(
                            amount = amount.toDouble(),
                            currency = "BDT",
                            category = selectedCategory,
                            merchant = merchant.ifBlank { null },
                            notes = notes.ifBlank { null }
                        )
                        amount = ""
                        merchant = ""
                        notes = ""
                        // Show success toast
                        Toast.makeText(context, "✓ Expense Saved Successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please enter an amount", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(secondaryGradient)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "Save Expense",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseScreenPreview() {
    ExpenseScreen()
}