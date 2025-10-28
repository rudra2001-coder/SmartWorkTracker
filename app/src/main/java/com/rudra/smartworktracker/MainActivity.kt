package com.rudra.smartworktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rudra.smartworktracker.ui.navigation.MainApp
import com.rudra.smartworktracker.ui.theme.SmartWorkTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartWorkTrackerMain()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SmartWorkTrackerMain() {
    SmartWorkTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            MainApp()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    SmartWorkTrackerMain()
}