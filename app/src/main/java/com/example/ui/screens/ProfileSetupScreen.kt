package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    user: User,
    onProfileCompleted: (User) -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf(user.fullName) }
    var selectedRole by remember { mutableStateOf(UserRole.GUEST) }

    val isFormValid = fullName.trim().isNotEmpty()

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                // Return completion callback
                onProfileCompleted(state.user)
                viewModel.resetState()
            }
            is AuthUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_setup_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Description Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "Welcome to Gusto Meets!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("profile_welcome_title")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Let's complete your profile to explore Chennai's finest privately hosted residential terraces.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("profile_welcome_subtitle")
                )
            }

            // Central Form Input Block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, DividerColor, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = "Your Profile Info",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Filled style input with person icon
                TextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Bhavana Chandra") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "User Icon", tint = TextSecondary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SlateBackground,
                        unfocusedContainerColor = SlateBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("full_name_input")
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Your Primary Role",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Custom Card Selections instead of standard radio buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Guest Option Card
                    val isGuestSelected = selectedRole == UserRole.GUEST
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isGuestSelected) OrangePrimary.copy(alpha = 0.08f) else SlateBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isGuestSelected) 2.dp else 1.dp,
                                color = if (isGuestSelected) OrangePrimary else DividerColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedRole = UserRole.GUEST }
                            .testTag("role_guest_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "Guest Icon",
                                tint = if (isGuestSelected) OrangePrimary else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "I'm a Guest",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGuestSelected) OrangePrimary else TextPrimary
                                )
                                Text(
                                    text = "Book terraces",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Host Option Card
                    val isHostSelected = selectedRole == UserRole.HOST
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHostSelected) TealAccent.copy(alpha = 0.08f) else SlateBackground
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = if (isHostSelected) 2.dp else 1.dp,
                                color = if (isHostSelected) TealAccent else DividerColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedRole = UserRole.HOST }
                            .testTag("role_host_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Host Icon",
                                tint = if (isHostSelected) TealAccent else TextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "I'm a Host",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHostSelected) TealAccent else TextPrimary
                                )
                                Text(
                                    text = "List my spaces",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Continue Submit Button
            Button(
                onClick = {
                    if (isFormValid) {
                        viewModel.completeProfile(
                            id = user.id,
                            phone = user.phoneNumber,
                            fullName = fullName,
                            role = selectedRole
                        )
                    }
                },
                enabled = isFormValid && authState !is AuthUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = OrangePrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("profile_commit_button")
            ) {
                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Continue to Home",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
