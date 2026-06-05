package com.example.domain.model

data class User(
    val id: String,
    val phoneNumber: String,
    val fullName: String,
    val role: UserRole,
    val kycVerified: Boolean,
    val walletBalance: Double
)

data class Terrace(
    val id: String,
    val hostId: String,
    val title: String,
    val description: String,
    val addressLine: String,
    val area: String,
    val city: String,
    val geoLat: Double,
    val geoLng: Double,
    val accessCategory: AccessType,
    val maxCapacity: Int,
    val baseHourlyRate: Double,
    val minBookingHours: Int,
    val photos: List<String>,
    val verificationStatus: VerificationStatus,
    val isActive: Boolean,
    val permissions: TerracePermissions
)

data class TerracePermissions(
    val allowAlcohol: Boolean,
    val allowSmoking: Boolean,
    val allowLoudMusic: Boolean,
    val allowOutsideFood: Boolean,
    val allowCouples: Boolean,
    val allowedPurposes: List<BookingPurpose>,
    val partyMultiplier: Double,
    val alcoholDeposit: Double
)

data class Booking(
    val id: String,
    val guestId: String,
    val terraceId: String,
    val startTime: Long,
    val endTime: Long,
    val actualCheckoutTime: Long?,
    val purpose: BookingPurpose,
    val guestCount: Int,
    val status: BookingStatus,
    val hourlyRateApplied: Double,
    val totalTimeCost: Double,
    val securityDepositHeld: Double,
    val platformFee: Double,
    val overstayPenalty: Double,
    val razorpayOrderId: String?,
    val digitalWaiverSigned: Boolean,
    val hostCheckedIn: Boolean
)

data class WalletTransaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val balanceAfter: Double,
    val description: String,
    val bookingId: String?,
    val createdAt: Long
)
