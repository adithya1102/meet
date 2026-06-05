package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.BookingResult
import com.example.data.repository.MeetsRepository
import com.example.lib.utils.ValidationUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class MeetsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = MeetsRepository(database.meetsDao())

    // --- CONTEXT STATE ---
    private val _currentUserId = MutableStateFlow("guest_id_default")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    fun setCurrentUserId(id: String) {
        _currentUserId.value = id
    }

    // Expose current user entity
    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { id ->
        repository.getUserFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Switching Roles on the fly for complete platform demonstration
    fun switchRole(newRole: String) {
        viewModelScope.launch {
            when (newRole) {
                "GUEST" -> {
                    _currentUserId.value = "guest_id_default"
                    _successMessage.value = "Switched to Guest Role (Bhavana Chandra)"
                }
                "HOST" -> {
                    _currentUserId.value = "host_id_1"
                    _successMessage.value = "Switched to Host Role (Adithya Narayanan C.)"
                }
                "ADMIN" -> {
                    _currentUserId.value = "admin_id_1"
                    _successMessage.value = "Switched to Corporate Admin Role"
                }
            }
        }
    }

    // --- EXPLORE & FILTERING STATE ---
    private val _selectedArea = MutableStateFlow("All")
    val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPurposeFilter = MutableStateFlow<String?>(null)
    val selectedPurposeFilter: StateFlow<String?> = _selectedPurposeFilter.asStateFlow()

    // Filters: Active premium terraces
    val filteredTerraces: StateFlow<List<TerraceEntity>> = combine(
        _selectedArea.flatMapLatest { area -> repository.getTerracesByArea(area) },
        _searchQuery,
        _selectedPurposeFilter
    ) { terraces, query, purpose ->
        terraces.filter { terrace ->
            val matchesQuery = terrace.title.contains(query, ignoreCase = true) ||
                    terrace.description.contains(query, ignoreCase = true) ||
                    terrace.area.contains(query, ignoreCase = true)

            val permissions = repository.getPermission(terrace.id)
            val matchesPurpose = if (purpose != null) {
                // Approximate matches or features based on selected purpose
                when (purpose) {
                    "PARTY" -> permissions?.partyMultiplier != null
                    "CHILLOUT" -> permissions?.allowOutsideFood == true
                    "DINE_OUT" -> terrace.baseHourlyRate >= 200
                    "MOVIE_NIGHT" -> permissions?.allowLoudMusic == true || terrace.maxCapacity >= 10
                    else -> true
                }
            } else true

            matchesQuery && matchesPurpose
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- BOOKINGS FLOW ---
    val bookings: StateFlow<List<BookingEntity>> = _currentUserId.flatMapLatest { id ->
        val user = repository.getUser(id)
        if (user?.role == "HOST") {
            repository.getBookingsForHost(id)
        } else {
            repository.getBookingsForGuest(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBookings: StateFlow<List<BookingEntity>> = repository.getAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- LEDGER WALLET STATE ---
    val walletBalance: StateFlow<Double> = _currentUserId.flatMapLatest { id ->
        repository.getWalletBalanceFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val walletTransactions: StateFlow<List<WalletTransactionEntity>> = _currentUserId.flatMapLatest { id ->
        repository.getWalletTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- DISPUTES STATE ---
    val damageReports: StateFlow<List<DamageReportEntity>> = repository.getAllDamageReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- SYSTEM ALERTS & MESSAGES ---
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun clearFeedback() {
        _successMessage.value = null
        _errorMessage.value = null
    }

    // --- SEED SEED DATABASE ON INIT ---
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- USER PROFILE ACTIONS & KYC ---
    private val _kycState = MutableStateFlow<String>("IDLE") // IDLE, RUNNING, SUCCESS, ERROR
    val kycState: StateFlow<String> = _kycState.asStateFlow()

    // KYC demo Aadhaar verification
    fun verifyUserAadhaar(aadhaar: String, inputFullName: String) {
        viewModelScope.launch {
            _kycState.value = "RUNNING"
            clearFeedback()

            // 1. Client side Verhoeff algorithm validation
            if (!ValidationUtils.validateAadhaar(aadhaar)) {
                _kycState.value = "ERROR"
                _errorMessage.value = "KYC Failure: Invalid Aadhaar number layout (Failed Verhoeff Checksum)."
                return@launch
            }

            // 2. Simulated Setu Sandbox No-OTP Demographic verification call
            // We sleep for 1000ms to simulate REST Web Service latency
            kotlinx.coroutines.delay(1200)

            val guest = currentUser.value ?: return@launch
            
            // Fuzzy match the Aadhaar legal record name with input name
            val isMatched = ValidationUtils.areNamesFuzzyMatching(guest.fullName.substringBefore(" ("), inputFullName)
            if (!isMatched) {
                _kycState.value = "ERROR"
                _errorMessage.value = "KYC Match Mismatch: Aadhaar Registry name does not fuzzy-match profile input name."
                return@launch
            }

            // Successfully matched! Update user verification status in DB
            val updatedUser = guest.copy(
                kycVerified = true,
                kycReferenceToken = "REF-SETU-" + UUID.randomUUID().toString().take(12).uppercase(),
                kycVerifiedName = inputFullName
            )
            repository.saveUser(updatedUser)
            _kycState.value = "SUCCESS"
            _successMessage.value = "Aadhaar verified successfully! KYC Identity Token successfully linked securely."
        }
    }

    // --- WALLET TOP UP ACTION ---
    fun depositFunds(amount: Double) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val rndPaymentId = "PAY-RZR-" + UUID.randomUUID().toString().take(6).uppercase()
            repository.topUpWallet(userId, amount, rndPaymentId)
            _successMessage.value = "Top Up Successful! Credited ₹$amount to wallet ledger via Razorpay (Reference: $rndPaymentId)"
        }
    }

    // --- BOOKING OPERATIONS ---
    fun createTerraceBooking(
        terrace: TerraceEntity,
        startTime: Long,
        endTime: Long,
        purpose: String,
        guestCount: Int,
        onResult: (BookingResult) -> Unit
    ) {
        viewModelScope.launch {
            clearFeedback()

            // Ensure profile is KYC verified before checkout
            val user = currentUser.value
            if (user == null || !user.kycVerified) {
                val error = BookingResult.Error("Security Requirement: Guest Profile must be Aadhaar KYC Verified to book private terraces.")
                onResult(error)
                _errorMessage.value = error.message
                return@launch
            }

            // 1. Calculate price with multipliers
            val permissions = repository.getPermission(terrace.id)
            val baseRate = terrace.baseHourlyRate
            val hours = (endTime - startTime).toDouble() / 3600000.0

            val multiplier = when (purpose) {
                "PARTY" -> permissions?.partyMultiplier ?: 1.00
                "DINE_OUT" -> 1.05
                "MOVIE_NIGHT" -> 1.10
                else -> 1.00
            }

            val rateApplied = baseRate * multiplier
            val timeCost = rateApplied * hours
            val securityHeld = permissions?.alcoholDeposit ?: 0.0
            val fee = timeCost * 0.10 // 10% platform fee
            val totalCost = timeCost + securityHeld + fee

            // Run central ledger creation call
            val result = repository.createBooking(
                guestId = user.id,
                terraceId = terrace.id,
                startTime = startTime,
                endTime = endTime,
                purpose = purpose,
                guestCount = guestCount,
                hourlyRate = rateApplied,
                securityDeposit = securityHeld,
                platformFee = fee,
                totalCost = totalCost,
                totalHours = hours,
                totalTimeCost = timeCost
            )

            when (result) {
                is BookingResult.Success -> {
                    _successMessage.value = "Booking Secured! Allocated Booking ID: ${result.booking.id}. Funds captured in Escrow."
                    onResult(result)
                }
                is BookingResult.Overlap -> {
                    _errorMessage.value = "Booking Conflict: Overlapping reservation exists. Selected slot must have 30-minute buffer buffer turnover."
                    onResult(result)
                }
                is BookingResult.InsufficientFunds -> {
                    _errorMessage.value = "Insufficient Balance: Wallet has ₹${result.activeBalance}. Checkout requires ₹${String.format("%.2f", result.required)}. Standard top-up required."
                    onResult(result)
                }
                is BookingResult.Error -> {
                    _errorMessage.value = result.message
                    onResult(result)
                }
            }
        }
    }

    // Checking guest in (Host Dashboard action)
    fun triggerCheckIn(bookingId: String) {
        viewModelScope.launch {
            repository.checkInGuest(bookingId)
            _successMessage.value = "Guest session checked in! Recording active start time."
        }
    }

    // Extending active session on the fly
    fun triggerExtension(bookingId: String, extraHours: Double, paymentMethod: String) {
        viewModelScope.launch {
            clearFeedback()
            val booking = repository.getBookingFlow(bookingId).first() ?: return@launch
            
            // Calculate extra rate and payment
            val extraCost = booking.hourlyRateApplied * extraHours

            if (paymentMethod == "Razorpay Input") {
                // Add credits first, simulating instant payment success
                val rndPaymentId = "PAY-RZR-EXT-" + UUID.randomUUID().toString().take(6).uppercase()
                repository.topUpWallet(booking.guestId, extraCost, rndPaymentId)
            }

            val result = repository.extendBooking(bookingId, extraHours, extraCost)
            when (result) {
                is BookingResult.Success -> {
                    _successMessage.value = "Awesome! Extended session by $extraHours hour(s) securely."
                }
                is BookingResult.Overlap -> {
                    _errorMessage.value = "Cannot Extend: The subsequent slot has an overlapping booking turnover buffer."
                }
                is BookingResult.InsufficientFunds -> {
                    _errorMessage.value = "Insufficient Balance: Top up wallet ledger first to extend session."
                }
                is BookingResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
        }
    }

    // Force checkout (complete session) from host or guest
    fun completeSession(bookingId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            clearFeedback()
            repository.completeBooking(bookingId, rating, comment)
            _successMessage.value = "Session completed successfully. Hosted Escrow security deposit has been refunded to guest wallet."
        }
    }

    // --- OVERSTAY CHECK ACTION (Admin or manual test function) ---
    fun applyOverstayPenalty(bookingId: String) {
        viewModelScope.launch {
            val penalty = repository.checkAndApplyOverstay(bookingId)
            if (penalty > 0.0) {
                _successMessage.value = "Applied Overstay Penalty: ₹$penalty debited from Guest Escrow Security Deposit and credited to Host wallet!"
            } else {
                _errorMessage.value = "No penalty applicable. Session is still within booking schedule parameters."
            }
        }
    }

    // --- DAMAGE REPORT OPERATIONS ---
    fun fileDamageClaim(bookingId: String, description: String, claimedAmount: Double) {
        viewModelScope.launch {
            val hostId = _currentUserId.value
            repository.fileDamageReport(bookingId, hostId, description, claimedAmount)
            _successMessage.value = "Damage Dispute Filed! Frozen Guest's Escrow Security Deposit of ₹$claimedAmount pending Admin Resolution."
        }
    }

    fun resolveDispute(reportId: String, note: String, approveClaim: Boolean) {
        viewModelScope.launch {
            repository.resolveDamageDispute(reportId, note, approveClaim)
            val action = if (approveClaim) "APPROVED" else "REJECTED"
            _successMessage.value = "Damage Dispute RESOLVED. Status: $action. Payout settlement adjusted in ledger."
        }
    }

    fun approveTerraceVerification(terraceId: String) {
        viewModelScope.launch {
            repository.verifyTerrace(terraceId, true)
            _successMessage.value = "Terrace Listed Approved! Parapet height of >4.0ft wall verified successfully. Now active."
        }
    }

    fun rejectTerraceVerification(terraceId: String) {
        viewModelScope.launch {
            repository.verifyTerrace(terraceId, false)
            _errorMessage.value = "Terrace safety inspection criteria failed. Listing set to Suspended."
        }
    }

    // Public setter methods for Composable flows to avoid reflection
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedArea(area: String) {
        _selectedArea.value = area
    }

    fun updateSelectedPurposeFilter(purpose: String?) {
        _selectedPurposeFilter.value = purpose
    }

    fun updateTerraceConfig(terraceId: String, newRate: Double, newCapacity: Int) {
        viewModelScope.launch {
            val terrace = repository.getTerrace(terraceId) ?: return@launch
            val p = repository.getPermission(terraceId) ?: TerracePermissionEntity(terraceId)
            repository.saveTerrace(
                terrace.copy(baseHourlyRate = newRate, maxCapacity = newCapacity),
                p
            )
            _successMessage.value = "Terrace Configuration Saved Successfully!"
        }
    }
}
