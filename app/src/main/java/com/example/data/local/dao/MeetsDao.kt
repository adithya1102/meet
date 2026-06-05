package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetsDao {

    // --- USERS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserFlow(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>


    // --- TERRACES & PERMISSIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerrace(terrace: TerraceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: TerracePermissionEntity)

    @Query("SELECT * FROM terraces WHERE isActive = 1 OR verificationStatus = 'PENDING_INSPECTION'")
    fun getAllTerracesFlow(): Flow<List<TerraceEntity>>

    @Query("SELECT * FROM terraces WHERE id = :id")
    suspend fun getTerraceById(id: String): TerraceEntity?

    @Query("SELECT * FROM terrace_permissions WHERE terraceId = :id")
    suspend fun getPermissionByTerraceId(id: String): TerracePermissionEntity?

    @Query("SELECT * FROM terraces WHERE area = :area AND (isActive = 1 OR verificationStatus = 'PENDING_INSPECTION')")
    fun getTerracesByArea(area: String): Flow<List<TerraceEntity>>


    // --- BOOKINGS & PREVENT DOUBLE BOOKING ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingFlow(bookingId: String): Flow<BookingEntity?>

    @Query("SELECT * FROM bookings WHERE guestId = :guestId ORDER BY startTime DESC")
    fun getBookingsForGuestFlow(guestId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE terraceId IN (SELECT id FROM terraces WHERE hostId = :hostId) ORDER BY startTime DESC")
    fun getBookingsForHostFlow(hostId: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings ORDER BY startTime DESC")
    fun getAllBookingsFlow(): Flow<List<BookingEntity>>

    // Overlap Check query: Double-Booking Prevention.
    // Checks if there are ANY bookings for `terraceId` overlapping [S_new, E_new]
    // with status indicating reserved/occupied (CONFIRMED, ACTIVE, EXTENDED, OVERSTAYED, PENDING_PAYMENT)
    // Overlap condition: S_new < E_old AND E_new > S_old
    @Query("""
        SELECT COUNT(*) FROM bookings 
        WHERE terraceId = :terraceId 
        AND status IN ('CONFIRMED', 'ACTIVE', 'EXTENDED', 'OVERSTAYED', 'PENDING_PAYMENT')
        AND :startTime < endTime 
        AND :endTime > startTime
    """)
    suspend fun countOverlappingBookings(terraceId: String, startTime: Long, endTime: Long): Int


    // --- IMMUTABLE LEDGER WALLET ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWalletTransaction(tx: WalletTransactionEntity)

    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE userId = :userId")
    suspend fun getWalletBalance(userId: String): Double?

    @Query("SELECT SUM(amount) FROM wallet_transactions WHERE userId = :userId")
    fun getWalletBalanceFlow(userId: String): Flow<Double?>

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getWalletTransactionsFlow(userId: String): Flow<List<WalletTransactionEntity>>


    // --- DAMAGE REPORTS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDamageReport(report: DamageReportEntity)

    @Query("SELECT * FROM damage_reports WHERE bookingId = :bookingId")
    suspend fun getDamageReportByBookingId(bookingId: String): DamageReportEntity?

    @Query("SELECT * FROM damage_reports ORDER BY createdAt DESC")
    fun getAllDamageReportsFlow(): Flow<List<DamageReportEntity>>


    // --- REVIEWS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    @Query("SELECT * FROM reviews WHERE revieweeId = :userId ORDER BY createdAt DESC")
    fun getReviewsForUserFlow(userId: String): Flow<List<ReviewEntity>>
}
