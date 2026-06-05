package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onNavigateToOtp: (String) -> Unit,
    onLoginSuccess: (com.example.domain.model.User, Boolean) -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val authState by viewModel.uiState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    val isPhoneValid = phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.OtpSent -> {
                onNavigateToOtp(state.phoneNumber)
                viewModel.resetState()
            }
            is AuthUiState.Success -> {
                onLoginSuccess(state.user, state.isNewUser)
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
            .testTag("auth_screen_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section - Branding & Greet Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Text(
                    text = "Gusto Meets",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary,
                    modifier = Modifier.testTag("auth_logo_text")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Celebrate & Socialize on Premier Terraces",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("auth_tagline_text")
                )
            }

            // Middle Section - Interactive Form inputs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurface, shape = RoundedCornerShape(24.dp))
                    .border(1.dp, DividerColor, shape = RoundedCornerShape(24.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log In or Sign Up",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // PhoneNumber Input with +91 Country Indicator
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { input ->
                        if (input.length <= 10 && input.all { it.isDigit() }) {
                            phoneNumber = input
                        }
                    },
                    label = { Text("Phone Number") },
                    placeholder = { Text("Enter 10-digit number") },
                    leadingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                        ) {
                            Text(
                                text = "+91",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp)
                                    .background(DividerColor)
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Phone icon",
                            tint = TextSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phone_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Send OTP Button
                Button(
                    onClick = {
                        if (isPhoneValid && activity != null) {
                            viewModel.sendOtp("+91$phoneNumber", activity)
                        }
                    },
                    enabled = isPhoneValid && authState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        disabledContainerColor = OrangePrimary.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("send_otp_button")
                ) {
                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Send OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Elegant modern divider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
                    Text(
                        text = "OR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Modern androidx.credentials Google Sign In Button
                OutlinedButton(
                    onClick = {
                        viewModel.signInWithGoogle(context)
                    },
                    enabled = authState !is AuthUiState.Loading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_signin_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // High Contrast text indicating google authenticator
                        Text(
                            text = "Continue with Google",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Bottom Section - Privacy & Safety notes
            Text(
                text = "By continuing, you agree to our Terms of Service & Privacy Policy.",
                fontSize = 11.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}
