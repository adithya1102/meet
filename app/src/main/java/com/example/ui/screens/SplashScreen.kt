package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirebaseAuthDataSource
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: (String) -> Unit,
    onNavigateToAuth: () -> Unit,
    authDataSource: FirebaseAuthDataSource,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000) // Beautiful brand recognition pause
        val isLoggedIn = authDataSource.isUserLoggedIn()
        val currentUserId = authDataSource.currentUserToken
        if (isLoggedIn && currentUserId != null) {
            onNavigateToHome(currentUserId)
        } else {
            onNavigateToAuth()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground)
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = startAnimation,
            enter = fadeIn(animationSpec = tween(1200))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // High contrast aesthetic typography & logo pairing
                Icon(
                    imageVector = Icons.Default.HomeRepairService,
                    contentDescription = "Gusto Meets Icon",
                    tint = OrangePrimary,
                    modifier = Modifier
                        .size(80.dp)
                        .testTag("splash_brand_icon")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Gusto Meets",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.testTag("splash_title_text")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hourly Terrace Rentals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    modifier = Modifier.testTag("splash_subtitle_text")
                )
                Spacer(modifier = Modifier.height(48.dp))
                CircularProgressIndicator(
                    color = OrangePrimary,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("splash_progress_indicator")
                )
            }
        }
    }
}
