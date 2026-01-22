package com.rudra.smartworktracker.ui.screens.scheduler

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class RingtonePickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                RingtonePickerScreen(
                    onRingtoneSelected = { uri, name ->
                        val resultIntent = Intent().apply {
                            putExtra("ringtone_uri", uri?.toString())
                            putExtra("ringtone_name", name)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonePickerScreen(
    onRingtoneSelected: (Uri?, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    
    val ringtones = remember {
        val manager = RingtoneManager(context)
        manager.setType(RingtoneManager.TYPE_ALARM)
        val cursor = manager.cursor
        val list = mutableListOf<Pair<Uri?, String>>()
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val uri = manager.getRingtoneUri(cursor.position)
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                list.add(uri to title)
            }
        }
        list
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Ringtone") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Default option
            item {
                ListItem(
                    headlineContent = { Text("Default") },
                    supportingContent = { Text("System default alarm sound") },
                    leadingContent = {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        onRingtoneSelected(null, "Default")
                    }
                )
                HorizontalDivider()
            }
            
            items(ringtones) { (uri, title) ->
                ListItem(
                    headlineContent = { Text(title) },
                    leadingContent = {
                        RadioButton(
                            selected = uri == selectedUri,
                            onClick = { selectedUri = uri }
                        )
                    },
                    modifier = Modifier.clickable {
                        selectedUri = uri
                        onRingtoneSelected(uri, title)
                    }
                )
                HorizontalDivider()
            }
        }
    }
}
