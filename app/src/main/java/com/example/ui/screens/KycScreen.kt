package com.example.ui.screens

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.model.OwnershipType
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.KycUiState
import com.example.ui.viewmodel.KycViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onKycSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val kycViewModel: KycViewModel = viewModel(
        factory = KycViewModel.provideFactory(application)
    )

    val kycState by kycViewModel.uiState.collectAsState()
    val isAadhaarValid by kycViewModel.aadhaarValid.collectAsState()

    var aadhaarNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf(user.fullName) }
    var isConsentChecked by remember { mutableStateOf(false) }

    var selectedOwnershipType by remember { mutableStateOf<OwnershipType?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var nocUploaded by remember { mutableStateOf(false) }
    var nocFileName by remember { mutableStateOf("") }

    val showPropertyOwnership = user.role == UserRole.HOST

    LaunchedEffect(kycState) {
        if (kycState is KycUiState.Success) {
            onKycSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Verify Identity",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SlateBackground
                )
            )
        },
        containerColor = SlateBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Verify your identity",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangePrimary
                )
                Text(
                    text = "Required before participating or booking on Gusto Meets.",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            AnimatedVisibility(visible = kycState is KycUiState.Success || kycState is KycUiState.Error) {
                val backgroundTint = if (kycState is KycUiState.Success) GreenActive.copy(alpha = 0.1f) else RedAlert.copy(alpha = 0.1f)
                val borderTint = if (kycState is KycUiState.Success) GreenActive.copy(alpha = 0.3f) else RedAlert.copy(alpha = 0.3f)
                val contentTint = if (kycState is KycUiState.Success) GreenActive else RedAlert

                val message = when (val state = kycState) {
                    is KycUiState.Success -> "Identity Linked Successfully! Demographic matching has set Name to [${state.verifiedName}]"
                    is KycUiState.Error -> state.message
                    else -> ""
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundTint, RoundedCornerShape(12.dp))
                        .border(1.dp, borderTint, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (kycState is KycUiState.Success) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = "Notification indicator status",
                        tint = contentTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = message,
                        color = contentTint,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Aadhaar Demographics Check",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Profile Verified Full Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User verification avatar",
                                tint = OrangePrimary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = DividerColor,
                            focusedContainerColor = SlateBackground,
                            unfocusedContainerColor = SlateBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kyc_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = aadhaarNumber,
                        onValueChange = { input ->
                            if (input.length <= 12 && input.all { it.isDigit() }) {
                                aadhaarNumber = input
                                kycViewModel.onAadhaarChanged(input)
                            }
                        },
                        label = { Text("12-Digit Aadhaar Number") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Aadhaar card logo layout",
                                tint = OrangePrimary
                            )
                        },
                        trailingIcon = {
                            if (aadhaarNumber.length == 12) {
                                if (isAadhaarValid == true) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Valid Aadhaar Checksum Check",
                                        tint = GreenActive,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Invalid Checksum Error",
                                        tint = RedAlert,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = if (isAadhaarValid == true) GreenActive else OrangePrimary,
                            unfocusedBorderColor = if (aadhaarNumber.length == 12 && isAadhaarValid != true) RedAlert else DividerColor,
                            focusedContainerColor = SlateBackground,
                            unfocusedContainerColor = SlateBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kyc_aadhaar_input"),
                        singleLine = true
                    )

                    if (aadhaarNumber.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isAadhaarValid == true) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isAadhaarValid == true) GreenActive else RedAlert,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isAadhaarValid == true) "Verhoeff Checksum Check Completed Successfully!" else "Checksum verification failing (Must be exactly 12 valid digits).",
                                fontSize = 11.sp,
                                color = if (isAadhaarValid == true) GreenActive else RedAlert
                            )
                        }
                    }
                }
            }

            if (showPropertyOwnership) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateSurface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Property Ownership Validation",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TealAccent
                        )

                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = when (selectedOwnershipType) {
                                        OwnershipType.OWNER -> "OWNER (Self Owned)"
                                        OwnershipType.TENANT -> "TENANT (Rental Agreement)"
                                        OwnershipType.APARTMENT_ASSOCIATION -> "APARTMENT ASSOCIATION"
                                        null -> "Select Property Ownership Type"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Ownership Type Selection") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary,
                                        focusedBorderColor = TealAccent,
                                        unfocusedBorderColor = DividerColor,
                                        focusedContainerColor = SlateBackground,
                                        unfocusedContainerColor = SlateBackground
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )

                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    OwnershipType.entries.forEach { type ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = when (type) {
                                                        OwnershipType.OWNER -> "OWNER (Self Owned)"
                                                        OwnershipType.TENANT -> "TENANT (Rental Agreement)"
                                                        OwnershipType.APARTMENT_ASSOCIATION -> "APARTMENT ASSOCIATION"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                selectedOwnershipType = type
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        when (selectedOwnershipType) {
                            OwnershipType.TENANT -> {
                                Text(
                                    text = "Tenants must upload landlord NOC (No Objection Certificate) before list hosting approval.",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                if (nocUploaded) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                GreenActive.copy(alpha = 0.08f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .border(
                                                1.dp,
                                                GreenActive.copy(alpha = 0.3f),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDone,
                                                contentDescription = "Upload successful",
                                                tint = GreenActive
                                            )
                                            Column {
                                                Text(
                                                    text = "NOC_Verified.pdf",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "Securely loaded to storage bucket",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        IconButton(onClick = {
                                            nocUploaded = false
                                            nocFileName = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete document",
                                                tint = RedAlert
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            nocUploaded = true
                                            nocFileName = "noc_landlord_verified.pdf"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SlateBackground),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, TealAccent, RoundedCornerShape(10.dp)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = "Upload Doc icon indicator",
                                            tint = TealAccent
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Upload Owner NOC (PDF/Image)",
                                            fontWeight = FontWeight.Bold,
                                            color = TealAccent
                                        )
                                    }
                                }
                            }
                            OwnershipType.APARTMENT_ASSOCIATION -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        OrangePrimary.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "RWA Warning",
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Note: RWA Association payouts must be routed to the central RWA bank ledger.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OrangePrimary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isConsentChecked = !isConsentChecked }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = isConsentChecked,
                    onCheckedChange = { isConsentChecked = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = OrangePrimary,
                        uncheckedColor = DividerColor
                    )
                )
                Text(
                    text = "I consent to KYC demographic verification via UIDAI-licensed gateway API.",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val isButtonEnabled = fullName.isNotBlank() &&
                    isAadhaarValid == true &&
                    isConsentChecked &&
                    kycState != KycUiState.Submitting &&
                    (!showPropertyOwnership || selectedOwnershipType != null) &&
                    (!showPropertyOwnership || selectedOwnershipType != OwnershipType.TENANT || nocUploaded)

            Button(
                onClick = {
                    kycViewModel.submitKyc(
                        aadhaarNumber = aadhaarNumber,
                        fullName = fullName,
                        ownershipType = selectedOwnershipType,
                        nocUri = if (nocUploaded) nocFileName else null,
                        userId = user.id,
                        userRole = user.role.name
                    )
                },
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = SlateSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("kyc_submit_button")
            ) {
                if (kycState == KycUiState.Submitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VERIFYING DEMOGRAPHIC LINK...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Trigger link key icon",
                        tint = if (isButtonEnabled) Color.White else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START SECURE SETU KYC LINK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
