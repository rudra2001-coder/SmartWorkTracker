package com.rudra.smartworktracker.ui.screens.team

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.SharedPreferenceManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreen() {
    val context = LocalContext.current
    val sharedPreferenceManager = SharedPreferenceManager(context)
    val teamViewModel: TeamViewModel = viewModel(
        factory = TeamViewModelFactory(sharedPreferenceManager)
    )

    var teamName by remember { mutableStateOf("") }
    var teammateName by remember { mutableStateOf("") }
    var teammateNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Teams")
        LazyColumn {
            items(teamViewModel.teams.value) { team ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Team: ${team.name}")
                        team.teammates.forEach { teammate ->
                            Text("  - ${teammate.name}: ${teammate.number}")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Add Team")
        TextField(value = teamName, onValueChange = { teamName = it }, label = { Text("Team Name") })
        Button(onClick = { teamViewModel.addTeam(Team(teamName, emptyList())) }) {
            Text("Add Team")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Add Teammate")
        TextField(value = teammateName, onValueChange = { teammateName = it }, label = { Text("Teammate Name") })
        TextField(value = teammateNumber, onValueChange = { teammateNumber = it }, label = { Text("Teammate Number") })
        Row {
            teamViewModel.teams.value.forEach { team ->
                Button(onClick = { teamViewModel.addTeammate(team.name, Teammate(teammateName, teammateNumber)) }) {
                    Text("Add to ${team.name}")
                }
            }
        }
    }
}
