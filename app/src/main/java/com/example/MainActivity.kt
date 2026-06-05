package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FirebaseAuthDataSource
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MeetsViewModel

class MainActivity : ComponentActivity() {

    private val authDataSource by lazy { com.example.di.ServiceLocator.firebaseAuthDataSource }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables Android modern Edge-To-Edge content drawing out of the box
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val meetsViewModel: MeetsViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)

                // High fidelity state-based routing system
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedPhoneNumber by remember { mutableStateOf("") }
                var pendingUser by remember { mutableStateOf<com.example.domain.model.User?>(null) }

                when (currentScreen) {
                    "splash" -> {
                        SplashScreen(
                            onNavigateToHome = { userId ->
                                meetsViewModel.setCurrentUserId(userId)
                                currentScreen = "home"
                            },
                            onNavigateToAuth = {
                                currentScreen = "auth"
                            },
                            authDataSource = authDataSource
                        )
                    }
                    "auth" -> {
                        AuthScreen(
                            onNavigateToOtp = { phone ->
                                selectedPhoneNumber = phone
                                currentScreen = "otp"
                            },
                            onLoginSuccess = { user, isNewUser ->
                                if (isNewUser) {
                                    pendingUser = user
                                    currentScreen = "profile_setup"
                                } else {
                                    meetsViewModel.setCurrentUserId(user.id)
                                    currentScreen = "home"
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                    "otp" -> {
                        OtpVerificationScreen(
                            phoneNumber = selectedPhoneNumber,
                            onBack = {
                                currentScreen = "auth"
                            },
                            onVerificationSuccess = { user, isNewUser ->
                                if (isNewUser) {
                                    pendingUser = user
                                    currentScreen = "profile_setup"
                                } else {
                                    meetsViewModel.setCurrentUserId(user.id)
                                    currentScreen = "home"
                                }
                            },
                            viewModel = authViewModel
                        )
                    }
                    "profile_setup" -> {
                        pendingUser?.let { user ->
                            ProfileSetupScreen(
                                user = user,
                                onProfileCompleted = { completedUser ->
                                    meetsViewModel.setCurrentUserId(completedUser.id)
                                    currentScreen = "home"
                                },
                                viewModel = authViewModel
                            )
                        }
                    }
                    "home" -> {
                        GustoMeetsApp(viewModel = meetsViewModel)
                    }
                }
            }
        }
    }
}
