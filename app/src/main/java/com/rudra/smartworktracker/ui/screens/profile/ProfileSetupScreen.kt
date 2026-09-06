package com.rudra.smartworktracker.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import com.rudra.smartworktracker.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: UserProfileViewModel = viewModel(
        factory = UserProfileViewModel.Factory(
            UserProfileRepository(AppDatabase.getDatabase(context).userProfileDao())
        )
    )
    
    val profileState by viewModel.profileState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var skillInput by remember { mutableStateOf("") }
    val skills = remember { mutableStateListOf<String>() }
    
    var monthlySalary by remember { mutableStateOf("") }
    var initialSavings by remember { mutableStateOf("") }

    LaunchedEffect(profileState) {
        if (profileState is UserProfileViewModel.ProfileState.Success) {
            val profile = (profileState as UserProfileViewModel.ProfileState.Success).profile
            name = profile.name
            email = profile.email
            phone = profile.phone
            bio = profile.bio
            location = profile.location
            experience = profile.experience
            monthlySalary = profile.monthlySalary.toString()
            initialSavings = profile.initialSavings.toString()
            skills.clear()
            skills.addAll(profile.skills)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (profileState is UserProfileViewModel.ProfileState.Success) "Edit Profile" else "Setup Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = experience,
                onValueChange = { experience = it },
                label = { Text("Experience") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = monthlySalary,
                    onValueChange = { monthlySalary = it },
                    label = { Text("Monthly Salary") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
                )
                OutlinedTextField(
                    value = initialSavings,
                    onValueChange = { initialSavings = it },
                    label = { Text("Initial Savings") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null) }
                )
            }

            // Skills
            Text("Skills", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = skillInput,
                    onValueChange = { skillInput = it },
                    label = { Text("Add Skill") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (skillInput.isNotBlank()) {
                        skills.add(skillInput)
                        skillInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Skill")
                }
            }

            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skills.forEach { skill ->
                    InputChip(
                        selected = true,
                        onClick = { skills.remove(skill) },
                        label = { Text(skill) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            Button(
                onClick = {
                    val profile = UserProfile(
                        name = name,
                        email = email,
                        phone = phone,
                        bio = bio,
                        location = location,
                        experience = experience,
                        skills = skills.toList(),
                        monthlySalary = monthlySalary.toDoubleOrNull() ?: 0.0,
                        initialSavings = initialSavings.toDoubleOrNull() ?: 0.0
                    )
                    viewModel.saveProfile(profile)
                    onProfileSaved()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Save Profile")
            }
        }
    }
}
