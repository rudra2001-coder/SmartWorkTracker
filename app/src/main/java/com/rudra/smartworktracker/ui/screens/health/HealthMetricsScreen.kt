package com.rudra.smartworktracker.ui.screens.health

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.model.HealthMetricType

@Composable
fun HealthMetricsScreen(viewModel: HealthMetricsViewModel = viewModel()) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (weight.isNotBlank()) {
                    viewModel.saveHealthMetric(HealthMetricType.WEIGHT, weight.toDouble())
                    Toast.makeText(context, "Weight Saved", Toast.LENGTH_SHORT).show()
                    weight = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Weight")
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (height.isNotBlank()) {
                    viewModel.saveHealthMetric(HealthMetricType.HEIGHT, height.toDouble())
                    Toast.makeText(context, "Height Saved", Toast.LENGTH_SHORT).show()
                    height = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Height")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthMetricsScreenPreview() {
    HealthMetricsScreen()
}
