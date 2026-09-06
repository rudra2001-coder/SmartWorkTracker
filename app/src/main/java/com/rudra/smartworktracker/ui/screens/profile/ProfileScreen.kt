package com.rudra.smartworktracker.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rudra.smartworktracker.data.AppDatabase
import com.rudra.smartworktracker.data.entity.UserProfile
import com.rudra.smartworktracker.data.repository.UserProfileRepository
import com.rudra.smartworktracker.ui.components.AppColors
import com.rudra.smartworktracker.ui.components.SectionHeader
import com.rudra.smartworktracker.ui.components.StandardCard
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
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val repository = UserProfileRepository(database.userProfileDao())
    val viewModel: UserProfileViewModel = viewModel(factory = UserProfileViewModel.Factory(repository))
    
    val profileState by viewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Profile", 
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.PrimaryText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppColors.PrimaryText)
                    }
                },
                actions = {
                    if (profileState is UserProfileViewModel.ProfileState.Success) {
                        IconButton(onClick = onNavigateToSetup) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = AppColors.PrimaryText)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.GlobalBackground,
                    titleContentColor = AppColors.PrimaryText
                )
            )
        },
        containerColor = AppColors.GlobalBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = profileState) {
                is UserProfileViewModel.ProfileState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AppColors.OfficeBlue
                    )
                }
                is UserProfileViewModel.ProfileState.Success -> {
                    ProfileContent(state.profile)
                }
                is UserProfileViewModel.ProfileState.NotCreated -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No profile found",
                            color = AppColors.SecondaryText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSetup,
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OfficeBlue)
                        ) {
                            Text("Set Up Profile", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
                is UserProfileViewModel.ProfileState.Error -> {
                    Text(
                        text = state.message,
                        color = AppColors.ExpenseRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileContent(profile: UserProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, CardShape, clip = false),
            shape = CardShape,
            elevation = CardDefaults.cardElevation(0.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(AppColors.OfficeBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.name.isNotEmpty()) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = AppColors.OfficeBlue
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = AppColors.OfficeBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = profile.name.ifEmpty { "User Name" },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryText
                )

                if (profile.bio.isNotEmpty()) {
                    Text(
                        text = profile.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppColors.SecondaryText,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Section
        SectionHeader(text = "Contact Information")
        StandardCard(modifier = Modifier.shadow(6.dp, CardShape, clip = false)) {
            ProfileInfoItem(Icons.Default.Email, "Email", profile.email)
            ProfileInfoItem(Icons.Default.Phone, "Phone", profile.phone)
            ProfileInfoItem(Icons.Default.LocationOn, "Location", profile.location)
        }

        Spacer(modifier = Modifier.height(16.dp))

        SectionHeader(text = "Professional")
        StandardCard(modifier = Modifier.shadow(6.dp, CardShape, clip = false)) {
            ProfileInfoItem(Icons.Default.Work, "Experience", profile.experience)
        }

        if (profile.skills.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(text = "Skills")
            StandardCard(modifier = Modifier.shadow(6.dp, CardShape, clip = false)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profile.skills.forEach { skill ->
                        AssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    skill,
                                    color = AppColors.PrimaryText
                                ) 
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = AppColors.GlobalBackground
                            )
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { content() }
    )
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    if (value.isEmpty()) return
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(ChipShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(SapphireBlue, EmeraldGreen)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label, 
                style = MaterialTheme.typography.labelSmall, 
                color = AppColors.OfficeBlue
            )
            Text(
                value, 
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.PrimaryText
            )
        }
    }
}
