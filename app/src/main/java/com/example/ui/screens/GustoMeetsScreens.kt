package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.*
import com.example.data.repository.BookingResult
import com.example.lib.utils.ValidationUtils
import com.example.ui.theme.*
import com.example.ui.viewmodel.MeetsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GustoMeetsApp(viewModel: MeetsViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Screen states
    var guestTab by remember { mutableStateOf("explore") } // explore, bookings, wallet, kyc
    var hostTab by remember { mutableStateOf("dashboard") } // dashboard, listings
    var adminTab by remember { mutableStateOf("verifications") } // verifications, disputes, overstays

    // Dialog state
    var selectedTerraceForDetail by remember { mutableStateOf<TerraceEntity?>(null) }

    LaunchedEffect(successMessage, errorMessage) {
        if (successMessage != null || errorMessage != null) {
            delay(4000)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(SlateBackground)) {
                // Main Header Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GUSTO ECOSYSTEM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Gusto Meets",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Compact Wallet Quick Balance badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                if (currentUser?.role == "GUEST") guestTab = "wallet"
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = "Wallet logo",
                                    tint = TealAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "₹${String.format("%.0f", walletBalance)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TealAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Custom Designed Profile Avatar from HTML
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(SlateSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AN",
                                color = TealAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // ROLE SWITCHER BAR (Essential for marketplace test audit)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val roles = listOf("GUEST", "HOST", "ADMIN")
                        roles.forEach { role ->
                            val isActive = currentUser?.role == role
                            val activeColor = when (role) {
                                "GUEST" -> OrangePrimary
                                "HOST" -> TealAccent
                                else -> GreenActive
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isActive) activeColor.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable {
                                        viewModel.switchRole(role)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = role,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp,
                                    color = if (isActive) activeColor else TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // KYC Status Indicator Badge
                currentUser?.let { user ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(
                                color = if (user.kycVerified) GreenActive.copy(alpha = 0.08f) else RedAlert.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (user.kycVerified) GreenActive.copy(alpha = 0.15f) else RedAlert.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (user.kycVerified) Icons.Default.VerifiedUser else Icons.Default.GppMaybe,
                                contentDescription = "KYC status Indicator",
                                tint = if (user.kycVerified) GreenActive else RedAlert,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (user.kycVerified) {
                                    "Aadhaar KYC Verified: ${user.kycVerifiedName ?: user.fullName}"
                                } else {
                                    "Aadhaar KYC Verification required to book."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.kycVerified) GreenActive else RedAlert
                            )
                        }
                    }
                }
                
                // Feedback message toast alert overlays
                AnimatedVisibility(visible = successMessage != null || errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (successMessage != null) GreenActive.copy(alpha = 0.15f) else RedAlert.copy(alpha = 0.15f))
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (successMessage != null) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Overlay icon badge",
                            tint = if (successMessage != null) GreenActive else RedAlert,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = successMessage ?: errorMessage ?: "",
                            fontSize = 12.sp,
                            color = if (successMessage != null) GreenActive else RedAlert,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Context-specific bottom bar
            when (currentUser?.role) {
                "GUEST" -> {
                    Column {
                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = SlateSurface
                        ) {
                            val tabs = listOf(
                                Triple("explore", Icons.Default.Explore, "Explore"),
                                Triple("bookings", Icons.Default.Book, "Bookings"),
                                Triple("wallet", Icons.Default.AccountBalanceWallet, "Wallet"),
                                Triple("kyc", Icons.Default.AssignmentInd, "KYC")
                            )
                            tabs.forEach { (tab, icon, label) ->
                                NavigationBarItem(
                                    selected = guestTab == tab,
                                    onClick = { guestTab = tab },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OrangePrimary,
                                        selectedTextColor = OrangePrimary,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = OrangePrimary.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
                "HOST" -> {
                    Column {
                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = SlateSurface
                        ) {
                            val tabs = listOf(
                                Triple("dashboard", Icons.Default.SpaceDashboard, "My Dashboard"),
                                Triple("listings", Icons.Default.Roofing, "My Listings")
                            )
                            tabs.forEach { (tab, icon, label) ->
                                NavigationBarItem(
                                    selected = hostTab == tab,
                                    onClick = { hostTab = tab },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = TealAccent,
                                        selectedTextColor = TealAccent,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = TealAccent.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
                "ADMIN" -> {
                    Column {
                        Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(DividerColor))
                        NavigationBar(
                            windowInsets = WindowInsets.navigationBars,
                            containerColor = SlateSurface
                        ) {
                            val tabs = listOf(
                                Triple("verifications", Icons.Default.Verified, "Inspections"),
                                Triple("disputes", Icons.Default.Gavel, "Live Disputes"),
                                Triple("overstays", Icons.Default.Timer, "Overstays")
                            )
                            tabs.forEach { (tab, icon, label) ->
                                NavigationBarItem(
                                    selected = adminTab == tab,
                                    onClick = { adminTab = tab },
                                    icon = { Icon(icon, contentDescription = label) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = GreenActive,
                                        selectedTextColor = GreenActive,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = GreenActive.copy(alpha = 0.12f)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
        ) {
            when (currentUser?.role) {
                "GUEST" -> {
                    when (guestTab) {
                        "explore" -> ExploreScreen(viewModel) { selectedTerraceForDetail = it }
                        "bookings" -> GuestBookingsScreen(viewModel)
                        "wallet" -> WalletScreen(viewModel)
                        "kyc" -> KycScreen(viewModel)
                    }
                }
                "HOST" -> {
                    when (hostTab) {
                        "dashboard" -> HostDashboardScreen(viewModel)
                        "listings" -> HostListingsScreen(viewModel)
                    }
                }
                "ADMIN" -> {
                    when (adminTab) {
                        "verifications" -> AdminVerificationsScreen(viewModel)
                        "disputes" -> AdminDisputesScreen(viewModel)
                        "overstays" -> AdminOverstaysScreen(viewModel)
                    }
                }
            }
        }
    }

    // Detail overlay modal
    selectedTerraceForDetail?.let { terrace ->
        TerraceDetailDialog(
            terrace = terrace,
            viewModel = viewModel,
            onDismiss = { selectedTerraceForDetail = null }
        )
    }
}

// ==========================================
// GUEST: EXPLORE SCREEN
// ==========================================
@Composable
fun ExploreScreen(
    viewModel: MeetsViewModel,
    onTerraceClick: (TerraceEntity) -> Unit
) {
    val terraces by viewModel.filteredTerraces.collectAsState()
    val areaFilter by viewModel.selectedArea.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val purposeFilter by viewModel.selectedPurposeFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search areas, terraces, features...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search bar icon label") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SlateSurface,
                unfocusedContainerColor = SlateSurface,
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = DividerColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("explore_search_bar")
                .clip(RoundedCornerShape(12.dp)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick area filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val areas = listOf("All", "Velachery", "Perungudi", "OMR")
            areas.forEach { area ->
                val isSelected = areaFilter == area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) OrangePrimary else SlateSurface)
                        .clickable { viewModel.updateSelectedArea(area) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = area,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Purpose Filters Dropdown selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateSurface, RoundedCornerShape(10.dp))
                .clickable {
                    val nextPurpose = when (purposeFilter) {
                        null -> "PARTY"
                        "PARTY" -> "CHILLOUT"
                        "CHILLOUT" -> "DINE_OUT"
                        "DINE_OUT" -> "MOVIE_NIGHT"
                        else -> null
                    }
                    viewModel.updateSelectedPurposeFilter(nextPurpose)
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Event mode filter label",
                    tint = TealAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Event Mode: ${purposeFilter ?: "Global (All)"}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Tap to cycle",
                fontSize = 11.sp,
                color = TealAccent,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Terrace Listings Column
        if (terraces.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = "Empty list view indicator icon",
                        tint = TextSecondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No active terraces found.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Try adjusting your area or event filters.",
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(terraces) { terrace ->
                    TerraceCard(terrace = terrace, onClick = { onTerraceClick(terrace) })
                }
            }
        }
    }
}

@Composable
fun TerraceCard(terrace: TerraceEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("terrace_item_${terrace.id}"),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // High Density Thumbnail Indicator
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                OrangePrimary.copy(alpha = 0.82f),
                                SlateSurfaceElevated
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Roofing,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(44.dp)
                )
            }

            // Text side
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = terrace.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Rating Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(SlateBackground, RoundedCornerShape(12.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "4.8",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }

                    Text(
                        text = "${terrace.area} • Max ${terrace.maxCapacity} Guests",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Text(
                        text = terrace.description,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Rates & CTA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₹${terrace.baseHourlyRate}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangePrimary
                        )
                        Text(text = "/hr", fontSize = 10.sp, color = TextSecondary)
                    }

                    // Compact pill indicating safety height
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(GreenActive.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GreenActive,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${terrace.parapetHeightFt}ft",
                            fontSize = 10.sp,
                            color = GreenActive,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// GUEST: TERRACE DETAIL & BOOKING PANEL
// ==========================================
@Composable
fun TerraceDetailDialog(
    terrace: TerraceEntity,
    viewModel: MeetsViewModel,
    onDismiss: () -> Unit
) {
    var rentDateIndex by remember { mutableStateOf(0) } // 0: Today, 1: Tomorrow, 2: Day After
    var startHourIndex by remember { mutableStateOf(4) } // Index in hours list
    var durationHours by remember { mutableStateOf(2) } // Hours duration
    var purposeSelected by remember { mutableStateOf("CHILLOUT") } // CHILLOUT, PARTY, MOVIE_NIGHT, DINE_OUT
    var guestCount by remember { mutableStateOf(4) }

    val purposes = listOf("CHILLOUT", "PARTY", "MOVIE_NIGHT", "DINE_OUT", "BOARD_GAMES", "STUDY_GROUP")
    val hoursList = listOf("9:00 AM", "11:00 AM", "1:00 PM", "3:00 PM", "5:00 PM", "7:00 PM", "9:00 PM")

    val baseRate = terrace.baseHourlyRate
    val multiplier = when (purposeSelected) {
        "PARTY" -> 1.30
        "DINE_OUT" -> 1.05
        "MOVIE_NIGHT" -> 1.10
        else -> 1.00
    }
    val rateApplied = baseRate * multiplier
    val timeCost = rateApplied * durationHours
    val securityDeposit = if (purposeSelected == "PARTY") 500.0 else 200.0
    val platformFee = timeCost * 0.10
    val totalCost = timeCost + securityDeposit + platformFee

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .testTag("terrace_detail_panel"),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terrace Booking Grid",
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close description layout", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Text(
                            text = terrace.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Map location PIN", tint = TextSecondary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${terrace.addressLine}, ${terrace.area}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = terrace.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Features / Permissions grid
                        Text(text = "TERRACE CODES", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Parapet Safety Badge
                            Box(
                                modifier = Modifier
                                    .background(GreenActive.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = GreenActive, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Safe Height (${terrace.parapetHeightFt}ft)", color = GreenActive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Staircase Badge
                            Box(
                                modifier = Modifier
                                    .background(TealAccent.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = terrace.accessCategory.replace("_", " "),
                                    color = TealAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // STEP 1: Select Date
                        Text(text = "1. CHOOSE DATE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Today", "Tomorrow", "Day After").forEachIndexed { index, title ->
                                val isSelected = rentDateIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) OrangePrimary else SlateSurfaceElevated, RoundedCornerShape(8.dp))
                                        .clickable { rentDateIndex = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(title, color = if (isSelected) TextPrimary else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // STEP 2: Select Start Time
                        Text(text = "2. START HOUR", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cycle indices visually
                            hoursList.take(4).forEachIndexed { index, time ->
                                val isSelected = startHourIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) TealAccent else SlateSurfaceElevated, RoundedCornerShape(8.dp))
                                        .clickable { startHourIndex = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(time, color = if (isSelected) SlateBackground else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            // Button to cycle remaining
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(SlateSurfaceElevated, RoundedCornerShape(8.dp))
                                    .clickable { startHourIndex = (startHourIndex + 1) % hoursList.size }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("More...", color = TealAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // STEP 3: Duration & Capacity
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "3. HOURS SPAN", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.background(SlateSurfaceElevated, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { if (durationHours > 1) durationHours-- }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Less", tint = TextPrimary)
                                    }
                                    Text(
                                        text = "$durationHours hrs",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(onClick = { if (durationHours < 12) durationHours++ }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextPrimary)
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "GUESTS COUNT", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.background(SlateSurfaceElevated, RoundedCornerShape(8.dp)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { if (guestCount > 1) guestCount-- }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Less", tint = TextPrimary)
                                    }
                                    Text(
                                        text = "$guestCount pax",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                    IconButton(onClick = { if (guestCount < terrace.maxCapacity) guestCount++ }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = TextPrimary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // STEP 4: Event Category (Multipliers)
                        Text(text = "4. SOCIAL EVENT STYLE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Cycle visual selection
                            listOf("CHILLOUT", "PARTY", "MOVIE_NIGHT").forEach { purpose ->
                                val isSelected = purposeSelected == purpose
                                val mText = when (purpose) {
                                    "PARTY" -> "1.30x Multiplier"
                                    "MOVIE_NIGHT" -> "1.10x Multiplier"
                                    else -> "1.00x Base"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSelected) OrangePrimary.copy(alpha = 0.2f) else SlateSurfaceElevated, RoundedCornerShape(8.dp))
                                        .drawBehind {
                                            if (isSelected) {
                                                drawRoundRect(
                                                    color = OrangePrimary,
                                                    style = Stroke(width = 2.dp.toPx())
                                                )
                                            }
                                        }
                                        .clickable { purposeSelected = purpose }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(purpose, color = if (isSelected) OrangeLight else TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text(mText, color = TextSecondary.copy(alpha = 0.7f), fontSize = 8.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Dynamic Payout Breakdown Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("COSTING BREAKDOWN", fontSize = 10.sp, color = OrangeLight, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                CostRow(label = "Base Hourly Rate", value = "₹$baseRate/hr")
                                CostRow(label = "Applied Hourly Rate (Event Mode)", value = "₹${String.format("%.2f", rateApplied)}/hr")
                                CostRow(label = "Total Hours Span ($durationHours hrs)", value = "₹${String.format("%.2f", timeCost)}")
                                CostRow(label = "Escrow Security Deposit (Fully Refundable)", value = "₹${String.format("%.2f", securityDeposit)}")
                                CostRow(label = "Network Platform Fee (10%)", value = "₹${String.format("%.2f", platformFee)}")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom visual divider line
                                    Spacer(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Escrow Total Quote Due", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text("₹${String.format("%.2f", totalCost)}", fontWeight = FontWeight.Bold, color = OrangeLight, fontSize = 15.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // CTA booking button
                Button(
                    onClick = {
                        // Calculate timestamps
                        val baseTime = System.currentTimeMillis() + (rentDateIndex * 86400000L)
                        val startHourOffset = (startHourIndex * 2 + 9) * 3600000L
                        val startTimeStamp = (baseTime / 86400000L) * 86400000L + startHourOffset
                        val endTimeStamp = startTimeStamp + (durationHours * 3600000L).toLong()

                        viewModel.createTerraceBooking(
                            terrace = terrace,
                            startTime = startTimeStamp,
                            endTime = endTimeStamp,
                            purpose = purposeSelected,
                            guestCount = guestCount,
                            onResult = { result ->
                                if (result is BookingResult.Success) {
                                    onDismiss()
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("terrace_book_now_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SECURE TERRACE ESCROW • ₹${String.format("%.2f", totalCost)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// GUEST: CURRENT/PAST BOOKINGS LIST SCREEN
// ==========================================
@Composable
fun GuestBookingsScreen(viewModel: MeetsViewModel) {
    val bookings by viewModel.bookings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Escrow Bookings ledger",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "No Bookings registered label",
                        tint = TextSecondary,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No bookings, yet.", color = TextSecondary, fontSize = 14.sp)
                    Text("Explore terraces nearby around Chennai.", color = TextSecondary.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings) { booking ->
                    BookingItemCard(booking = booking, viewModel = viewModel, isHostView = false)
                }
            }
        }
    }
}

@Composable
fun BookingItemCard(booking: BookingEntity, viewModel: MeetsViewModel, isHostView: Boolean) {
    val context = LocalContext.current
    var terraceTitle by remember { mutableStateOf("Premium Terrace") }
    var terraceArea by remember { mutableStateOf("Chennai") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    var damageClaimAmount by remember { mutableStateOf("") }
    var damageDescription by remember { mutableStateOf("") }
    var showDamageClaimDialog by remember { mutableStateOf(false) }

    LaunchedEffect(booking.terraceId) {
        val t = viewModel.javaClass.getDeclaredField("repository").let { f ->
            f.isAccessible = true
            f.get(viewModel) as com.example.data.repository.MeetsRepository
        }.getTerrace(booking.terraceId)
        t?.let {
            terraceTitle = it.title
            terraceArea = it.area
        }
    }

    // Beautiful M3 state tags
    val statusColors = when (booking.status) {
        "CONFIRMED" -> Pair(GreenActive, "SCHEDULED")
        "ACTIVE", "EXTENDED" -> Pair(TealAccent, "SESSION IN LIVE COUNTDOWN")
        "COMPLETED" -> Pair(TextSecondary, "RESOLVED & RELEASED")
        "DISPUTED" -> Pair(RedAlert, "ESCROW DEPOSIT FROZEN / DISPUTED")
        "OVERSTAYED" -> Pair(YellowWarning, "OVERSTAYED PENALTY APPLIED")
        else -> Pair(TextSecondary, booking.status)
    }

    // Setup dates formatting
    val dfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val dfDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val startStr = dfTime.format(Date(booking.startTime))
    val endStr = dfTime.format(Date(booking.endTime))
    val dateStr = dfDate.format(Date(booking.startTime))

    val isActive = booking.status == "ACTIVE" || booking.status == "EXTENDED" || booking.status == "OVERSTAYED"

    if (isActive) {
        // High Density IMMERSIVE Active Session Card (inspired directly by the Design HTML)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_card_${booking.id}"),
            colors = CardDefaults.cardColors(containerColor = OrangePrimary),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Active status label & Time Left box
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Pulsing active dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(Color(0xFF4ADE80)) // green-400
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE SESSION",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = terraceTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Ends at $endStr • ${booking.guestCount} Guests",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                    }

                    // Countdown info badge
                    val remainingTime = booking.endTime - System.currentTimeMillis()
                    val minutesLeft = (remainingTime / 60000L).coerceAtLeast(0)
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MINUTES LEFT",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%02d", minutesLeft),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom subtle white line
                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))

                Spacer(modifier = Modifier.height(14.dp))

                // Dynamic Action buttons custom-themed with high contrast
                if (isHostView) {
                    // Host action
                    Button(
                        onClick = { showCheckoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = OrangePrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("COMPLETE & SECURE CHECKOUT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    // Guest action buttons (Extend & Directions)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.triggerExtension(booking.id, 1.0, "Wallet Ledger")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = OrangePrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("EXTEND +1 HR (₹${String.format("%.0f", booking.hourlyRateApplied)})", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                val latLong = "12.9784,80.2184"
                                val mapIntent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("google.navigation:q=$latLong")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(mapIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f), contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("DIRECTIONS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // High Density Standard Cards with thin outline
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_card_${booking.id}"),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(statusColors.first.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusColors.second,
                            color = statusColors.first,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = dateStr,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = terraceTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = "$terraceArea • ${booking.purpose} Event Split",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Active clock label", tint = TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$startStr - $endStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Total Paid: ₹${String.format("%.2f", booking.totalTimeCost + booking.securityDepositHeld + booking.platformFee)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = OrangePrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f).height(1.dp).background(DividerColor))
                }

                // Dynamic Action Panel context for non-active files
                if (isHostView) {
                    // Host Actions for Non-active
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (booking.status == "CONFIRMED") {
                            Button(
                                onClick = { viewModel.triggerCheckIn(booking.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated, contentColor = TealAccent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CHECK IN GUEST LIVE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        if (booking.status == "COMPLETED") {
                            Button(
                                onClick = { showDamageClaimDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = RedAlert.copy(alpha = 0.1f), contentColor = RedAlert),
                                border = androidx.compose.foundation.BorderStroke(1.dp, RedAlert.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text("REPORT / PROPERTY DISPUTE", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Guest Actions for Non-active (Just Directions badge)
                    Button(
                        onClick = {
                            val latLong = "12.9784,80.2184"
                            val mapIntent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("google.navigation:q=$latLong")
                            ).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(mapIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated, contentColor = TealAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = TealAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GET DIRECTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Checkout Review Dialog
    if (showCheckoutDialog) {
        Dialog(onDismissRequest = { showCheckoutDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Session Completion Survey", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangePrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Submit standard Guest performance and terrace check ratings to release held safety escrow credits.", fontSize = 12.sp, color = TextSecondary)
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Text("Rate Session Experience (1-5 Stars):", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (reviewRating >= star) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { reviewRating = star }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        placeholder = { Text("Write checkout check summary comments...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showCheckoutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated)
                        ) {
                            Text("BACK")
                        }
                        Button(
                            onClick = {
                                viewModel.completeSession(booking.id, reviewRating, reviewComment)
                                showCheckoutDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SUBMIT AND REFUND", color = SlateBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Damage Claim Dialog
    if (showDamageClaimDialog) {
        Dialog(onDismissRequest = { showDamageClaimDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("File Property Damage Dispute", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RedAlert)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Lock Guest's Held Escrow deposit of (up to ₹${booking.securityDepositHeld}) by filing high-fidelity audit evidence to corporate admin.", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = damageClaimAmount,
                        onValueChange = { damageClaimAmount = it },
                        placeholder = { Text("Damaged items cost value (INR)...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = RedAlert,
                            unfocusedBorderColor = DividerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = damageDescription,
                        onValueChange = { damageDescription = it },
                        placeholder = { Text("Aesthetic check description...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = RedAlert,
                            unfocusedBorderColor = DividerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showDamageClaimDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated)
                        ) {
                            Text("CANCEL")
                        }
                        Button(
                            onClick = {
                                val amount = damageClaimAmount.toDoubleOrNull() ?: 0.0
                                if (amount > 0.0) {
                                    viewModel.fileDamageClaim(booking.id, damageDescription, amount)
                                    showDamageClaimDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RedAlert),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SUBMIT TO ADMIN", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CircularTimerDraw(fraction: Float) {
    Canvas(modifier = Modifier.size(16.dp)) {
        drawCircle(color = TextSecondary.copy(alpha = 0.3f), style = Stroke(width = 2.dp.toPx()))
        drawArc(
            color = TealAccent,
            startAngle = -90f,
            sweepAngle = fraction * 360f,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

// ==========================================
// GUEST: IMMUTABLE LEDGER WALLET
// ==========================================
@Composable
fun WalletScreen(viewModel: MeetsViewModel) {
    val walletBalance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()
    var depositAmountInput by remember { mutableStateOf("1000") }
    var rzrOpenOtp by remember { mutableStateOf(false) }
    var rzrOtpChallengeInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Immutable Ledger Wallet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Every credit/debit transaction recorded sequentially in blockchain-style ledger.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Large balance Display Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("COMPUTABLE CRYPTO LEDGER BALANCE", fontSize = 10.sp, color = TealAccent, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "₹${String.format("%.2f", walletBalance)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Top Up Action panel
        Text("TOP UP WALLET LEDGER", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = depositAmountInput,
                onValueChange = { depositAmountInput = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = DividerColor
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Button(
                onClick = { rzrOpenOtp = true },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("CREDIT VIA RZR", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Ledger list
        Text("LEDGER AUDIT LOG", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No account mutations registered yet.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(transactions) { tx ->
                    val isCredit = tx.amount >= 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurface, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCredit) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (isCredit) GreenActive else RedAlert,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(tx.description, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    SimpleDateFormat("hh:mm a, MMM dd", Locale.getDefault()).format(Date(tx.createdAt)),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Text(
                            text = (if (isCredit) "+" else "") + "₹${String.format("%.2f", tx.amount)}",
                            fontWeight = FontWeight.Bold,
                            color = if (isCredit) GreenActive else RedAlert,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Razorpay checkout dialog check OTP code
    if (rzrOpenOtp) {
        Dialog(onDismissRequest = { rzrOpenOtp = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Secure Razorpay SDK Portal", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangePrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("UPI Intent sandbox simulation. Enter valid 4-digit MFA authorize pin '1234' to verify token signature and finalize transaction in Ledger.", fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = rzrOtpChallengeInput,
                        onValueChange = { rzrOtpChallengeInput = it },
                        placeholder = { Text("Verification OTP Code...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerColor
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (rzrOtpChallengeInput == "1234") {
                                val depositAmount = depositAmountInput.toDoubleOrNull() ?: 0.0
                                if (depositAmount > 0.0) {
                                    viewModel.depositFunds(depositAmount)
                                    rzrOpenOtp = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("AUTHORIZE & TRANSFER", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// GUEST: KYC VERIFICATION TAB
// ==========================================
@Composable
fun KycScreen(viewModel: MeetsViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val kycState by viewModel.kycState.collectAsState()
    var nameInput by remember { mutableStateOf("") }
    var aadhaarInput by remember { mutableStateOf("") }

    val isAadhaarLayoutValid = ValidationUtils.validateAadhaar(aadhaarInput)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Aadhaar Identity Linkage (KYC)",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Zero-Knowledge Demographic linkage powered by Setu Sandbox. Your actual Aadhaar storage is NEVER persisted locally.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        currentUser?.let { user ->
            if (user.kycVerified) {
                // Success Badge Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = GreenActive.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenActive, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IDENTITY COMPLIANT", color = GreenActive, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Aadhaar Ledger Verified Name: ${user.kycVerifiedName}", color = TextPrimary, fontSize = 12.sp)
                        Text("Secure Hash Token: ${user.kycReferenceToken}", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            } else {
                // Aadhaar application input
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Setu demographic validation form", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TealAccent)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Aadhaar Registered Full Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = DividerColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = aadhaarInput,
                            onValueChange = { if (it.length <= 12) aadhaarInput = it },
                            label = { Text("12-Digit Aadhaar Number") },
                            isError = aadhaarInput.isNotEmpty() && !isAadhaarLayoutValid,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = TealAccent,
                                unfocusedBorderColor = DividerColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // Live check indicator helper
                        if (aadhaarInput.isNotEmpty()) {
                            Text(
                                text = if (isAadhaarLayoutValid) "✓ Verhoeff Algorithm Checksum Valid!" else "✗ Checksum digit mismatched or length invalid",
                                fontSize = 10.sp,
                                color = if (isAadhaarLayoutValid) GreenActive else RedAlert,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (nameInput.isNotEmpty() && isAadhaarLayoutValid) {
                                    viewModel.verifyUserAadhaar(aadhaarInput, nameInput)
                                }
                            },
                            enabled = nameInput.isNotEmpty() && isAadhaarLayoutValid && kycState != "RUNNING",
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (kycState == "RUNNING") {
                                CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp))
                            } else {
                                Text("SECURE AUTHENTICATE DEMOGRAPHIC LINK", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HOST: DASHBOARD SCREEN
// ==========================================
@Composable
fun HostDashboardScreen(viewModel: MeetsViewModel) {
    val bookings by viewModel.bookings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Host Control Matrix",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Manage terrace access slots and verify checkout safety parameters.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No reservation tokens assigned to your listings.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(bookings) { booking ->
                    BookingItemCard(booking = booking, viewModel = viewModel, isHostView = true)
                }
            }
        }
    }
}

// ==========================================
// HOST: MY LISTINGS EDIT PANEL
// ==========================================
@Composable
fun HostListingsScreen(viewModel: MeetsViewModel) {
    val terraces by viewModel.filteredTerraces.collectAsState()
    var selectedListingForEdit by remember { mutableStateOf<TerraceEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Registered Terraces",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Edit base pricing rates and active safety permission policies.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(terraces) { terrace ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(terrace.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            IconButton(onClick = { selectedListingForEdit = terrace }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Listing parameters", tint = TealAccent)
                            }
                        }
                        Text("${terrace.area} • Fixed Base Hourly Rate: ₹${terrace.baseHourlyRate}/hr", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }

    selectedListingForEdit?.let { terrace ->
        EditListingDialog(
            terrace = terrace,
            viewModel = viewModel,
            onDismiss = { selectedListingForEdit = null }
        )
    }
}

@Composable
fun EditListingDialog(terrace: TerraceEntity, viewModel: MeetsViewModel, onDismiss: () -> Unit) {
    var rateInput by remember { mutableStateOf(terrace.baseHourlyRate.toString()) }
    var maxCapInput by remember { mutableStateOf(terrace.maxCapacity.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Terrace Configuration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TealAccent)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = rateInput,
                    onValueChange = { rateInput = it },
                    label = { Text("Base Hourly Rate (INR)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = maxCapInput,
                    onValueChange = { maxCapInput = it },
                    label = { Text("Maximum Guest Capacity") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = TealAccent,
                        unfocusedBorderColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceElevated)
                    ) {
                        Text("CANCEL")
                    }
                    Button(
                        onClick = {
                            val rate = rateInput.toDoubleOrNull() ?: terrace.baseHourlyRate
                            val cap = maxCapInput.toIntOrNull() ?: terrace.maxCapacity
                            viewModel.updateTerraceConfig(terrace.id, rate, cap)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("SAVE CONFIG", color = SlateBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMIN: VERIFICATIONS TAB PANEL
// ==========================================
@Composable
fun AdminVerificationsScreen(viewModel: MeetsViewModel) {
    val terraces by viewModel.filteredTerraces.collectAsState()
    val pendingListings = terraces.filter { it.verificationStatus == "PENDING_INSPECTION" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Safety Parapet Inspections Panel",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Inspections must verify legal parapet wall height values of at least >4.0 feet.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingListings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("All listings compliant. Perfect safety score.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingListings) { terrace ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(terrace.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Area: ${terrace.area} • Address: ${terrace.addressLine}", color = TextSecondary, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = GreenActive, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Audited Parapet Height: ${terrace.parapetHeightFt} feet",
                                    color = GreenActive,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.rejectTerraceVerification(terrace.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("REJECT / HALT", color = RedAlert, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { viewModel.approveTerraceVerification(terrace.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("APPROVE LISTING", color = SlateBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMIN: LIVE DISPUTE PANEL
// ==========================================
@Composable
fun AdminDisputesScreen(viewModel: MeetsViewModel) {
    val reports by viewModel.damageReports.collectAsState()
    val pendingDisputes = reports.filter { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Platform Damage Dispute Arbitration",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Arbitrate host damage claim payouts by allocating matching ledger debits.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingDisputes.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Zero pending damage reports to arbitrate.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(pendingDisputes) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Dispute Booking Ref: ${report.bookingId.take(8).uppercase()}", fontWeight = FontWeight.Bold, color = OrangeLight, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Claim Description: ${report.description}", color = TextPrimary, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Host Claim Amount:", fontSize = 11.sp, color = TextSecondary)
                                Text("₹${report.claimedAmount}", color = RedAlert, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.resolveDispute(report.id, "Rejected by Admin review", false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedAlert.copy(alpha = 0.2f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("REJECT / DISMISS", color = RedAlert, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { viewModel.resolveDispute(report.id, "Claim validated and processed by Admin", true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("APPROVE PAYOUT", color = SlateBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMIN: OVERSTAYS CHECKER PANEL
// ==========================================
@Composable
fun AdminOverstaysScreen(viewModel: MeetsViewModel) {
    val bookings by viewModel.allBookings.collectAsState()
    // Identify bookings which are still ACTIVE, but current time has crossed end time
    val overstayedBookings = bookings.filter { (it.status == "ACTIVE" || it.status == "EXTENDED") && System.currentTimeMillis() > it.endTime }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Platform Session Overstay Enforcer",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Overstay enforcer levies custom 2x hourly overstay penalties from Guest security deposits held in escrow.",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (overstayedBookings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Zero sessions have overstayed scheduled checks.", color = TextSecondary, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(overstayedBookings) { booking ->
                    val overstayMillis = System.currentTimeMillis() - booking.endTime
                    val extraMinutes = overstayMillis / 60000L
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SlateSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Overstayed Booking: ${booking.id.take(8).uppercase()}", fontWeight = FontWeight.Bold, color = RedAlert, fontSize = 14.sp)
                            Text("Scheduled Slot: ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.startTime))} - ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(booking.endTime))}", color = TextSecondary, fontSize = 12.sp)
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Elapsed Overstay Span: $extraMinutes Min",
                                color = RedAlert,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.applyOverstayPenalty(booking.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("EXECUTE 2X RATE LEDGER PENALTY DEBIT", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Cost Row
@Composable
fun CostRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

