package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    onBack: () -> Unit,
    onVerificationSuccess: (com.example.domain.model.User, Boolean) -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authState by viewModel.uiState.collectAsState()

    // 6-digit OTP fields state
    val otpValues = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Start verification check
    var timerSeconds by remember { mutableStateOf(60) }

    LaunchedEffect(timerSeconds) {
        if (timerSeconds > 0) {
            delay(1000)
            timerSeconds--
        }
    }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthUiState.Success -> {
                onVerificationSuccess(state.user, state.isNewUser)
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
        topBar = {
            TopAppBar(
                title = { Text("OTP Verification", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("otp_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Navigate back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SlateBackground)
            )
        },
        modifier = modifier.fillMaxSize()
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "A 6-digit verification code has been sent to",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = phoneNumber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.testTag("otp_phone_text")
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Segmented 6-digit code layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 0 until 6) {
                        OutlinedTextField(
                            value = otpValues[i],
                            onValueChange = { newValue ->
                                // Clean value to maximum of 1 digit
                                val cleaned = newValue.filter { it.isDigit() }.take(1)
                                otpValues[i] = cleaned

                                if (cleaned.isNotEmpty()) {
                                    // Auto-advance to the next field if not last
                                    if (i < 5) {
                                        focusRequesters[i + 1].requestFocus()
                                    } else {
                                        // Clear focus on last field
                                        focusManager.clearFocus()
                                    }
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = DividerColor
                            ),
                            modifier = Modifier
                                .width(46.dp)
                                .height(56.dp)
                                .focusRequester(focusRequesters[i])
                                .onKeyEvent { keyEvent ->
                                    // Handle Backspace for auto-retreat
                                    if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Backspace) {
                                        if (otpValues[i].isEmpty() && i > 0) {
                                            otpValues[i - 1] = ""
                                            focusRequesters[i - 1].requestFocus()
                                            true
                                        } else {
                                            false
                                        }
                                    } else {
                                        false
                                    }
                                }
                                .testTag("otp_digit_$i")
                        )
                    }
                }

                // Focus on the first field initially
                LaunchedEffect(Unit) {
                    focusRequesters[0].requestFocus()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Resend code timer or actionable text button
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timerSeconds > 0) {
                        Text(
                            text = "Resend OTP in $timerSeconds seconds",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            modifier = Modifier.testTag("otp_timer_text")
                        )
                    } else {
                        TextButton(
                            onClick = {
                                timerSeconds = 60
                                // Re-trigger send OTP from activity info
                                val act = context as? android.app.Activity
                                if (act != null) {
                                    viewModel.sendOtp(phoneNumber, act)
                                }
                            },
                            modifier = Modifier.testTag("otp_resend_button")
                        ) {
                            Text(
                                text = "Resend Code",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                    }
                }
            }

            // Bottom Confirm Button
            val codeEntered = otpValues.joinToString("")
            val isOtpComplete = codeEntered.length == 6

            Button(
                onClick = {
                    if (isOtpComplete) {
                        viewModel.verifyOtp(codeEntered)
                    }
                },
                enabled = isOtpComplete && authState !is AuthUiState.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = OrangePrimary.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("otp_verify_button")
            ) {
                if (authState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Verify & Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
