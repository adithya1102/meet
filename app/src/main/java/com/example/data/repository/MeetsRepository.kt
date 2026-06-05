package com.example.data.repository

import com.example.data.local.dao.MeetsDao
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.UUID

sealed class BookingResult {
    data class Success(val booking: BookingEntity) : BookingResult()
    object Overlap : BookingResult()
    data class InsufficientFunds(val required: Double, val activeBalance: Double) : BookingResult()
    data class Error(val message: String) : BookingResult()
}

class MeetsRepository(private val dao: MeetsDao) {

    // --- SEEDING DATA ---
    suspend fun seedDatabaseIfEmpty() {
        // Seed default users if empty
        val hostUser = dao.getUserById("host_id_1")
        if (hostUser == null) {
            dao.insertUser(
                UserEntity(
                    id = "host_id_1",
                    phoneNumber = "+919876543210",
                    fullName = "Adithya Narayanan C. (Host)",
                    role = "HOST",
                    kycVerified = true,
                    kycReferenceToken = "REF-AADHAAR-HOST-9999",
                    kycVerifiedName = "Adithya Narayanan C.",
                    completedBookings = 15,
                    avgHostRating = 4.8
                )
            )
        }

        val guestUser = dao.getUserById("guest_id_default")
        if (guestUser == null) {
            dao.insertUser(
                UserEntity(
                    id = "guest_id_default",
                    phoneNumber = "+919999888877",
                    fullName = "Bhavana Chandra (Guest)",
                    role = "GUEST",
                    kycVerified = false,
                    kycReferenceToken = null,
                    kycVerifiedName = null,
                    completedBookings = 0,
                    avgHostRating = 5.0
                )
            )
            // Pre-seed default user wallet with ₹2500 for a great test drive experience!
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    id = "init_tx_1",
                    userId = "guest_id_default",
                    amount = 2500.0,
                    balanceAfter = 2500.0,
                    description = "Welcome Promo Credits"
                )
            )
        }

        val adminUser = dao.getUserById("admin_id_1")
        if (adminUser == null) {
            dao.insertUser(
                UserEntity(
                    id = "admin_id_1",
                    phoneNumber = "+919000011111",
                    fullName = "Gusto Meets Admin Team",
                    role = "ADMIN",
                    kycVerified = true,
                    kycReferenceToken = "REF-ADMIN-8888",
                    kycVerifiedName = "Corporate Admin",
                    completedBookings = 0,
                    avgHostRating = 5.0
                )
            )
        }

        // Seed terraces if empty
        val tCheck = dao.getTerraceById("terrace_velachery_1")
        if (tCheck == null) {
            // Terrace 1
            dao.insertTerrace(
                TerraceEntity(
                    id = "terrace_velachery_1",
                    hostId = "host_id_1",
                    title = "Aura Sky Deck & Canopy Lounge",
                    description = "Beautiful premium private roof top terrace overlooking Velachery lake. Highly spacious with modular lounge chairs, soft ambient string lights, high brick styling, and a strict 4.6ft safety parapet concrete wall. Perfect for private family dinners or chill boards games nights with close friends.",
                    addressLine = "32, Lake View Avenue, Velachery Road",
                    city = "Chennai",
                    area = "Velachery",
                    geoLat = 12.9784,
                    geoLng = 80.2184,
                    accessCategory = "PRIVATE_STAIRCASE",
                    maxCapacity = 12,
                    baseHourlyRate = 250.0,
                    minBookingHours = 2,
                    parapetHeightFt = 4.6,
                    safetyVideoUrl = "https://example.com/videos/parapet_check_velachery.mp4",
                    isAffidavitSigned = true,
                    verificationStatus = "VERIFIED",
                    photos = "https://images.unsplash.com/photo-1533105079780-92b9be482077?w=500", // Will fallback/render standard drawable styling
                    isActive = true
                )
            )
            dao.insertPermission(
                TerracePermissionEntity(
                    terraceId = "terrace_velachery_1",
                    allowAlcohol = true,
                    allowSmoking = false,
                    allowLoudMusic = false,
                    allowOutsideFood = true,
                    allowCouples = true,
                    allowOvernight = false,
                    partyMultiplier = 1.25,
                    alcoholDeposit = 500.0
                )
            )

            // Terrace 2
            dao.insertTerrace(
                TerraceEntity(
                    id = "terrace_omr_2",
                    hostId = "host_id_1",
                    title = "The High Garden Perungudi",
                    description = "Lush green rooftop terrace right in Chennai's IT hub. Featuring rich grass carpeting, potted plants, custom warm aesthetic spotlight panels, private tables, and independent seating grids. Includes an verified 5ft parapet safety wall for stress-free gatherings.",
                    addressLine = "Plot 5B, Phase 2, MGR Main Road, Perungudi",
                    city = "Chennai",
                    area = "Perungudi",
                    geoLat = 12.9642,
                    geoLng = 80.2458,
                    accessCategory = "SHARED_WALKTHROUGH",
                    maxCapacity = 16,
                    baseHourlyRate = 200.0,
                    minBookingHours = 1,
                    parapetHeightFt = 5.0,
                    safetyVideoUrl = "https://example.com/videos/perungudi_height_audit.mp4",
                    isAffidavitSigned = true,
                    verificationStatus = "VERIFIED",
                    photos = "https://images.unsplash.com/photo-1504624263008-3f4bd9ec37a3?w=500",
                    isActive = true
                )
            )
            dao.insertPermission(
                TerracePermissionEntity(
                    terraceId = "terrace_omr_2",
                    allowAlcohol = false,
                    allowSmoking = true,
                    allowLoudMusic = true,
                    allowOutsideFood = true,
                    allowCouples = true,
                    allowOvernight = false,
                    partyMultiplier = 1.35,
                    alcoholDeposit = 0.0
                )
            )

            // Terrace 3
            dao.insertTerrace(
                TerraceEntity(
                    id = "terrace_adyar_3",
                    hostId = "host_id_1",
                    title = "Adyar Marina Sky Vista",
                    description = "A striking luxury modern deck offering stunning horizon ocean views. Elegant luxury lounge furniture, high-contrast black slate tables, safe glass steel railings matching 5.2 feet in height. Ideal for fine dine-out meetups and movie projection setups.",
                    addressLine = "9, Gandhi Nagar Crescent, Adyar",
                    city = "Chennai",
                    area = "OMR",
                    geoLat = 13.0063,
                    geoLng = 80.2522,
                    accessCategory = "PRIVATE_STAIRCASE",
                    maxCapacity = 20,
                    baseHourlyRate = 350.0,
                    minBookingHours = 2,
                    parapetHeightFt = 5.2,
                    safetyVideoUrl = null,
                    isAffidavitSigned = true,
                    verificationStatus = "VERIFIED",
                    photos = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=500",
                    isActive = true
                )
            )
            dao.insertPermission(
                TerracePermissionEntity(
                    terraceId = "terrace_adyar_3",
                    allowAlcohol = true,
                    allowSmoking = true,
                    allowLoudMusic = true,
                    allowOutsideFood = true,
                    allowCouples = true,
                    allowOvernight = true,
                    partyMultiplier = 1.40,
                    alcoholDeposit = 1000.0
                )
            )

            // Terrace 4 (Unverified, pending inspection for Admin to test review flow)
            dao.insertTerrace(
                TerraceEntity(
                    id = "terrace_unverified_4",
                    hostId = "host_id_1",
                    title = "OMR Sholi Woodside Canopy",
                    description = "An asset-rich terrace setting waiting, featuring a spectacular view and large open spacing. Has a safety inspection pending for its newly constructed 4.2 parapet wall. Admin review and verification required to make it active.",
                    addressLine = "Central Street, Sholinganallur",
                    city = "Chennai",
                    area = "OMR",
                    geoLat = 12.8974,
                    geoLng = 80.2281,
                    accessCategory = "SHARED_WALKTHROUGH",
                    maxCapacity = 8,
                    baseHourlyRate = 180.0,
                    minBookingHours = 1,
                    parapetHeightFt = 4.2,
                    safetyVideoUrl = "https://example.com/videos/sholi_video_walk.mp4",
                    isAffidavitSigned = true,
                    verificationStatus = "PENDING_INSPECTION",
                    photos = "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?w=500",
                    isActive = false
                )
            )
            dao.insertPermission(
                TerracePermissionEntity(
                    terraceId = "terrace_unverified_4",
                    allowAlcohol = false,
                    allowSmoking = false,
                    allowLoudMusic = true,
                    allowOutsideFood = true,
                    allowCouples = true,
                    allowOvernight = false,
                    partyMultiplier = 1.15,
                    alcoholDeposit = 0.0
                )
            )
        }
    }

    // --- USERS ---
    suspend fun getUser(id: String) = dao.getUserById(id)
    fun getUserFlow(id: String) = dao.getUserFlow(id)
    fun getAllUsersFlow() = dao.getAllUsersFlow()
    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)

    // --- TERRACES ---
    fun getAllTerraces() = dao.getAllTerracesFlow()
    fun getTerracesByArea(area: String) = if (area == "All") dao.getAllTerracesFlow() else dao.getTerracesByArea(area)
    suspend fun getTerrace(id: String) = dao.getTerraceById(id)
    suspend fun getPermission(terraceId: String) = dao.getPermissionByTerraceId(terraceId)
    suspend fun saveTerrace(terrace: TerraceEntity, permissions: TerracePermissionEntity) {
        dao.insertTerrace(terrace)
        dao.insertPermission(permissions)
    }

    // --- BOOKINGS & LEDGER ENGINE ---
    fun getBookingsForGuest(guestId: String) = dao.getBookingsForGuestFlow(guestId)
    fun getBookingsForHost(hostId: String) = dao.getBookingsForHostFlow(hostId)
    fun getAllBookings() = dao.getAllBookingsFlow()
    fun getBookingFlow(bookingId: String) = dao.getBookingFlow(bookingId)

    suspend fun checkOverlappingBookings(terraceId: String, startTime: Long, endTime: Long): Boolean {
        // Enforce a 30-minute buffer (1800000 millis) before and after to avoid tight turnovers
        val bufferTime = 1800000L
        return dao.countOverlappingBookings(
            terraceId,
            startTime - bufferTime,
            endTime + bufferTime
        ) > 0
    }

    suspend fun getWalletBalance(userId: String): Double {
        return dao.getWalletBalance(userId) ?: 0.0
    }

    fun getWalletBalanceFlow(userId: String): Flow<Double> {
        return dao.getWalletBalanceFlow(userId).map { it ?: 0.0 }
    }

    fun getWalletTransactions(userId: String) = dao.getWalletTransactionsFlow(userId)

    suspend fun topUpWallet(userId: String, amount: Double, paymentId: String) {
        val currentBalance = getWalletBalance(userId)
        val newBalance = currentBalance + amount
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = amount,
                balanceAfter = newBalance,
                description = "Wallet Top Up via Razorpay",
                razorpayPaymentId = paymentId
            )
        )
    }

    /**
     * Fully transactional booking reservation with double-booking prevention and wallet balance check.
     * Includes refundable security deposit logic held in escrow.
     */
    suspend fun createBooking(
        guestId: String,
        terraceId: String,
        startTime: Long,
        endTime: Long,
        purpose: String,
        guestCount: Int,
        hourlyRate: Double,
        securityDeposit: Double,
        platformFee: Double,
        totalCost: Double, // totalTimeCost + securityDeposit + platformFee
        totalHours: Double,
        totalTimeCost: Double
    ): BookingResult {
        // 1. Check double bookings
        val isOverlapped = checkOverlappingBookings(terraceId, startTime, endTime)
        if (isOverlapped) {
            return BookingResult.Overlap
        }

        // 2. Check wallet funds
        val activeBalance = getWalletBalance(guestId)
        if (activeBalance < totalCost) {
            return BookingResult.InsufficientFunds(required = totalCost, activeBalance = activeBalance)
        }

        val bookingId = UUID.randomUUID().toString()

        // 3. Imputable Ledger Debit
        val newBalance = activeBalance - totalCost
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = guestId,
                amount = -totalCost, // Debit is negative
                balanceAfter = newBalance,
                description = "Booking ID $bookingId and Escrow Deposit Held",
                bookingId = bookingId
            )
        )

        // 4. Create the booking entity
        val booking = BookingEntity(
            id = bookingId,
            guestId = guestId,
            terraceId = terraceId,
            startTime = startTime,
            endTime = endTime,
            purpose = purpose,
            guestCount = guestCount,
            status = "CONFIRMED", // Simulating instant Razorpay Webhook verification approval
            hourlyRateApplied = hourlyRate,
            totalHours = totalHours,
            totalTimeCost = totalTimeCost,
            securityDepositHeld = securityDeposit,
            platformFee = platformFee,
            razorpayPaymentId = "PAY-LOCAL-" + UUID.randomUUID().toString().take(6).uppercase()
        )

        dao.insertBooking(booking)
        return BookingResult.Success(booking)
    }

    suspend fun checkInGuest(bookingId: String) {
        val booking = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(
            booking.copy(
                status = "ACTIVE",
                hostCheckedIn = true,
                hostCheckedInAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun extendBooking(bookingId: String, extraHours: Double, extraCost: Double): BookingResult {
        val booking = dao.getBookingById(bookingId) ?: return BookingResult.Error("Booking not found")
        val currentEndTime = booking.endTime
        val newEndTime = currentEndTime + (extraHours * 3600000L).toLong()

        // Check if extended slot overlaps
        val isOverlapped = checkOverlappingBookings(booking.terraceId, currentEndTime, newEndTime)
        if (isOverlapped) {
            return BookingResult.Overlap
        }

        // Debit extra cost from wallet ledger
        val currentBalance = getWalletBalance(booking.guestId)
        if (currentBalance < extraCost) {
            return BookingResult.InsufficientFunds(required = extraCost, activeBalance = currentBalance)
        }

        val newBalance = currentBalance - extraCost
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = booking.guestId,
                amount = -extraCost,
                balanceAfter = newBalance,
                description = "Session Extension: Booking ID ${booking.id}",
                bookingId = booking.id
            )
        )

        dao.updateBooking(
            booking.copy(
                endTime = newEndTime,
                extensionsCount = booking.extensionsCount + 1,
                totalHours = booking.totalHours + extraHours,
                totalTimeCost = booking.totalTimeCost + extraCost,
                status = "EXTENDED"
            )
        )

        return BookingResult.Success(booking)
    }

    /**
     * Completes a booking and returns the held security deposit to guest's wallet ledger!
     */
    suspend fun completeBooking(bookingId: String, hostRating: Int, hostComment: String) {
        val booking = dao.getBookingById(bookingId) ?: return
        if (booking.status == "COMPLETED" || booking.status == "DISPUTED") return

        // 1. Change status to COMPLETED
        dao.updateBooking(
            booking.copy(
                status = "COMPLETED",
                actualCheckoutTime = System.currentTimeMillis()
            )
        )

        // 2. Bidirectional Review
        dao.insertReview(
            ReviewEntity(
                id = UUID.randomUUID().toString(),
                bookingId = bookingId,
                reviewerId = booking.guestId,
                revieweeId = booking.terraceId, // Rate the venue/host
                rating = hostRating,
                comment = hostComment
            )
        )

        // 3. Refund Security Deposit from Escrow to Guest Ledger
        if (booking.securityDepositHeld > 0.0) {
            val balance = getWalletBalance(booking.guestId)
            val newBalance = balance + booking.securityDepositHeld
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = booking.guestId,
                    amount = booking.securityDepositHeld, // Refund credit
                    balanceAfter = newBalance,
                    description = "Escrow Security Deposit Refund: Booking ID $bookingId",
                    bookingId = bookingId
                )
            )
        }

        // 4. Pay the Host (Total Time Cost minus Platform Fee) inside Host wallet
        val hostPayout = booking.totalTimeCost - booking.platformFee
        if (hostPayout > 0.0) {
            val hostId = dao.getTerraceById(booking.terraceId)?.hostId ?: "host_id_1"
            val hostBalance = getWalletBalance(hostId)
            val hostNewBalance = hostBalance + hostPayout
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = hostId,
                    amount = hostPayout,
                    balanceAfter = hostNewBalance,
                    description = "Payout for Booking ID $bookingId (Net Fee)",
                    bookingId = bookingId
                )
            )
        }
    }

    /**
     * Host invokes damage claim, freezing the security deposit from auto-refund, entering 'DISPUTED' state.
     */
    suspend fun fileDamageReport(bookingId: String, hostId: String, description: String, claimedAmount: Double) {
        val booking = dao.getBookingById(bookingId) ?: return
        dao.updateBooking(booking.copy(status = "DISPUTED"))

        dao.insertDamageReport(
            DamageReportEntity(
                id = UUID.randomUUID().toString(),
                bookingId = bookingId,
                hostId = hostId,
                description = description,
                photos = "https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=300", // simulated photo
                claimedAmount = claimedAmount,
                status = "PENDING"
            )
        )
    }

    // --- OVERSTAY ENGINE ---
    /**
     * Checks if active sessions have overstayed, applies 2x hourly overstay penalty,
     * deducts it from the escrow security deposit, and resolves the booking.
     */
    suspend fun checkAndApplyOverstay(bookingId: String): Double {
        val booking = dao.getBookingById(bookingId) ?: return 0.0
        val overstayTimeMillis = System.currentTimeMillis() - booking.endTime
        if (overstayTimeMillis <= 0) return 0.0

        val extraHours = overstayTimeMillis.toDouble() / 3600000.0
        val penaltyRate = booking.hourlyRateApplied * 2.0 // Double-rate penalty
        val overstayPenaltyValue = String.format("%.2f", extraHours * penaltyRate).toDouble()

        // Deduct penalty from held security deposit
        val maxAvailablePenalty = booking.securityDepositHeld
        val appliedPenalty = if (overstayPenaltyValue > maxAvailablePenalty) maxAvailablePenalty else overstayPenaltyValue
        val securityDepositRemaining = maxAvailablePenalty - appliedPenalty

        // Update booking with applied penalties
        dao.updateBooking(
            booking.copy(
                status = "OVERSTAYED",
                actualCheckoutTime = System.currentTimeMillis(),
                overstayPenalty = appliedPenalty
            )
        )

        // Ledger: Refund remaining deposit (if any) to Guest wallet
        if (securityDepositRemaining > 0.0) {
            val balance = getWalletBalance(booking.guestId)
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = booking.guestId,
                    amount = securityDepositRemaining,
                    balanceAfter = balance + securityDepositRemaining,
                    description = "Security Deposit Refund (Post-Overstay Debit): Booking ID $bookingId",
                    bookingId = bookingId
                )
            )
        }

        // Ledger: Pay the Host (Total Time Cost - Platform Fee + applied Penalty)
        val payoutToHost = (booking.totalTimeCost - booking.platformFee) + appliedPenalty
        val hostId = dao.getTerraceById(booking.terraceId)?.hostId ?: "host_id_1"
        val hostBalance = getWalletBalance(hostId)
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = hostId,
                amount = payoutToHost,
                balanceAfter = hostBalance + payoutToHost,
                description = "Payout post-overstay for Booking ID $bookingId",
                bookingId = bookingId
            )
        )

        return appliedPenalty
    }

    // --- ADMIN SYSTEM CONTROLS ---
    fun getAllDamageReports() = dao.getAllDamageReportsFlow()

    suspend fun resolveDamageDispute(reportId: String, adminNotes: String, approveClaim: Boolean) {
        val reportsList = dao.getAllDamageReportsFlow().firstOrNull() ?: return
        val report = reportsList.find { it.id == reportId } ?: return
        val booking = dao.getBookingById(report.bookingId) ?: return

        if (report.status != "PENDING") return

        val resolvedAmount = if (approveClaim) {
            // Deduct from deposit up to full security deposit limit
            val availableDeposit = booking.securityDepositHeld
            if (report.claimedAmount > availableDeposit) availableDeposit else report.claimedAmount
        } else {
            0.0
        }

        // 1. Update Damage Report
        dao.insertDamageReport(
            report.copy(
                status = if (approveClaim) "APPROVED" else "REJECTED",
                resolvedAmount = resolvedAmount,
                adminNotes = adminNotes,
                resolvedAt = System.currentTimeMillis()
            )
        )

        // 2. Update booking status
        dao.updateBooking(
            booking.copy(
                status = if (approveClaim) "COMPLETED" else "COMPLETED", // resolve dispute
                damagePenalty = resolvedAmount
            )
        )

        // 3. Ledger allocation:
        val securityDepositRemaining = booking.securityDepositHeld - resolvedAmount

        // 3a. Payout host the resolution amount + their booking fee
        val hostId = booking.terraceId.let { dao.getTerraceById(it)?.hostId } ?: "host_id_1"
        val hostBalance = getWalletBalance(hostId)
        val hostPayout = (booking.totalTimeCost - booking.platformFee) + resolvedAmount
        dao.insertWalletTransaction(
            WalletTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = hostId,
                amount = hostPayout,
                balanceAfter = hostBalance + hostPayout,
                description = "Dispute Resolved: Payout for Booking ID ${booking.id}",
                bookingId = booking.id
            )
        )

        // 3b. Refund remaining of deposit to guest description
        if (securityDepositRemaining > 0.0) {
            val guestBalance = getWalletBalance(booking.guestId)
            dao.insertWalletTransaction(
                WalletTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = booking.guestId,
                    amount = securityDepositRemaining,
                    balanceAfter = guestBalance + securityDepositRemaining,
                    description = "Security Deposit Refund (Post-Dispute Resolve): Booking ID ${booking.id}",
                    bookingId = booking.id
                )
            )
        }
    }

    suspend fun verifyTerrace(terraceId: String, approve: Boolean) {
        val terrace = dao.getTerraceById(terraceId) ?: return
        dao.insertTerrace(
            terrace.copy(
                verificationStatus = if (approve) "VERIFIED" else "SUSPENDED",
                isActive = approve
            )
        )
    }

    // --- REVIEWS ---
    fun getReviews(userId: String) = dao.getReviewsForUserFlow(userId)
}
