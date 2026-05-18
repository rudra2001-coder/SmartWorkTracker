package com.rudra.smartworktracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────────────────────
// COLOR PALETTE (Exact Codes from Design System)
// ─────────────────────────────────────────────────────────────────────────────

object AppColors {
    val GlobalBackground = Color(0xFFF8F9FA)      // Very light gray
    val CardBackground = Color(0xFFFFFFFF)       // Pure white
    val PrimaryText = Color(0xFF1E1E1E)            // Nearly black
    val SecondaryText = Color(0xFF8E8E93)         // Mid gray
    val IncomeGreen = Color(0xFF2ECC71)            // Income green
    val ExpenseRed = Color(0xFFFF6B6B)            // Expense red
    val OfficeBlue = Color(0xFF3498DB)            // Office blue
    val HomeMint = Color(0xFF1ABC9C)              // Home mint
    val OffPurple = Color(0xFF9B59B6)             // Off purple
}

// ─────────────────────────────────────────────────────────────────────────────
// REUSABLE COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * StandardCard - White card with rounded corners and shadow
 * - White background
 * - 16dp rounded corners
 * - 12dp padding
 * - 4dp elevation (ambient light shadow)
 * - 10dp margin bottom
 */
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

/**
 * SectionHeader - Section title with consistent styling
 * - Font: SemiBold
 * - Size: 18sp
 * - Color: #1E1E1E
 * - Padding: bottom 12dp
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = AppColors.PrimaryText
        ),
        modifier = modifier.padding(bottom = 12.dp)
    )
}

/**
 * StyledStatBox - Small white box with rounded corners for stats
 * - Small white box with rounded corners
 * - Text color matches the argument (e.g., Green for income, Red for expense)
 */
@Composable
fun StyledStatBox(
    text: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.SecondaryText
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            )
        }
    }
}

/**
 * StandardCardItem - A single item within a card, typically for list items
 * Used for settings items, transaction items, etc.
 */
@Composable
fun StandardCardItem(
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val modifierFinal = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    
    androidx.compose.foundation.layout.Row(
        modifier = modifierFinal
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        if (leadingContent != null) {
            leadingContent()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
        }
        
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.PrimaryText
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.SecondaryText
                )
            }
        }
        
        if (trailingContent != null) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}