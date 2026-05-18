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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.ui.components.AppColors
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import com.rudra.smartworktracker.viewmodel.UserProfileViewModel

private val CardShape = RoundedCornerShape(20.dp)
private val ChipShape = RoundedCornerShape(12.dp)
private val PillShape = RoundedCornerShape(50.dp)

private val EmeraldGreen = Color(0xFF00C896)
private val CoralRed = Color(0xFFFF5757)
private val SapphireBlue = Color(0xFF3B82F6)
private val GoldenAmber = Color(0xFFF59E0B)
private val VioletPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    onProfileSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = UserProfileRepository(database.userProfileDao())
    val viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.Factory(repository))
    
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(36.dp).clip(ChipShape)
                                    .background(Brush.linearGradient(colors = listOf(SapphireBlue, EmeraldGreen))),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(36.dp).clip(ChipShape)
                                    .background(Brush.linearGradient(colors = listOf(SapphireBlue, EmeraldGreen))),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(36.dp).clip(ChipShape)
                                    .background(Brush.linearGradient(colors = listOf(SapphireBlue, EmeraldGreen))),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        }
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Box(
                                modifier = Modifier.size(36.dp).clip(ChipShape)
                                    .background(Brush.linearGradient(colors = listOf(SapphireBlue, EmeraldGreen))),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        }
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = monthlySalary,
                            onValueChange = { monthlySalary = it },
                            label = { Text("Monthly Salary") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(ChipShape)
                                        .background(Brush.linearGradient(colors = listOf(GoldenAmber, CoralRed))),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                            }
                        )
                        OutlinedTextField(
                            value = initialSavings,
                            onValueChange = { initialSavings = it },
                            label = { Text("Initial Savings") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(ChipShape)
                                        .background(Brush.linearGradient(colors = listOf(EmeraldGreen, SapphireBlue))),
                                    contentAlignment = Alignment.Center
                                ) { Icon(Icons.Default.Savings, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, CardShape, clip = false),
                shape = CardShape,
                elevation = CardDefaults.cardElevation(0.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
