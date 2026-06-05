package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val phoneNumber: String,
    val fullName: String,
    val role: String, // GUEST, HOST, ADMIN
    val kycVerified: Boolean = false,
    val kycReferenceToken: String? = null,
    val kycVerifiedName: String? = null,
    val completedBookings: Int = 0,
    val avgHostRating: Double = 5.0
)

@Entity(tableName = "terraces")
data class TerraceEntity(
    @PrimaryKey val id: String,
    val hostId: String,
    val title: String,
    val description: String,
    val addressLine: String,
    val city: String = "Chennai",
    val area: String, // e.g., 'Velachery', 'OMR', 'Perungudi'
    val geoLat: Double,
    val geoLng: Double,
    val accessCategory: String, // PRIVATE_STAIRCASE, SHARED_WALKTHROUGH
    val maxCapacity: Int,
    val baseHourlyRate: Double,
    val minBookingHours: Int = 1,
    val parapetHeightFt: Double,
    val safetyVideoUrl: String? = null,
    val isAffidavitSigned: Boolean = false,
    val verificationStatus: String = "UNVERIFIED", // UNVERIFIED, PENDING_INSPECTION, VERIFIED, SUSPENDED
    val photos: String, // Comma separated image URL/identifiers
    val isActive: Boolean = false
)

@Entity(tableName = "terrace_permissions")
data class TerracePermissionEntity(
    @PrimaryKey val terraceId: String,
    val allowAlcohol: Boolean = false,
    val allowSmoking: Boolean = false,
    val allowLoudMusic: Boolean = false,
    val allowOutsideFood: Boolean = true,
    val allowCouples: Boolean = true,
    val allowOvernight: Boolean = false,
    val partyMultiplier: Double = 1.30,
    val alcoholDeposit: Double = 500.00
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val guestId: String,
    val terraceId: String,
    val startTime: Long, // Epoch millis
    val endTime: Long, // Epoch millis
    val actualCheckoutTime: Long? = null,
    val extensionsCount: Int = 0,
    val purpose: String, // CHILLOUT, DINE_OUT, MOVIE_NIGHT, PARTY, BOARD_GAMES, STUDY_GROUP
    val guestCount: Int,
    val status: String, // PENDING_PAYMENT, CONFIRMED, ACTIVE, EXTENDED, COMPLETED, OVERSTAYED, DISPUTED, CANCELLED
    val hourlyRateApplied: Double,
    val totalHours: Double,
    val totalTimeCost: Double,
    val securityDepositHeld: Double = 0.0,
    val platformFee: Double = 0.0,
    val overstayPenalty: Double = 0.0,
    val damagePenalty: Double = 0.0,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val digitalWaiverSigned: Boolean = false,
    val hostCheckedIn: Boolean = false,
    val hostCheckedInAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val reviewerId: String,
    val revieweeId: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double, // Positive = credit (top up, refund, payout), Negative = debit (booking payment, held deposit, penalties)
    val balanceAfter: Double,
    val description: String,
    val bookingId: String? = null,
    val razorpayPaymentId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "damage_reports")
data class DamageReportEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val hostId: String,
    val description: String,
    val photos: String, // Comma separated image URL/identifiers
    val claimedAmount: Double,
    val resolvedAmount: Double? = null,
    val adminNotes: String? = null,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)
