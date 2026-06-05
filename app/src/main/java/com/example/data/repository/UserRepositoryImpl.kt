package com.example.data.repository

import com.example.data.local.SessionDataStore
import com.example.data.local.dao.MeetsDao
import com.example.data.local.entity.UserEntity
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: MeetsDao,
    private val sessionDataStore: SessionDataStore,
    private val okHttpClient: OkHttpClient
) : UserRepository {

    override fun getUser(userId: String): Flow<User?> {
        val userFlow = dao.getUserFlow(userId)
        val balanceFlow = dao.getWalletBalanceFlow(userId)
        return combine(userFlow, balanceFlow) { entity, balance ->
            if (entity == null) null else {
                User(
                    id = entity.id,
                    phoneNumber = entity.phoneNumber,
                    fullName = entity.fullName,
                    role = when (entity.role) {
                        "HOST" -> UserRole.HOST
                        "ADMIN" -> UserRole.ADMIN
                        else -> UserRole.GUEST
                    },
                    kycVerified = entity.kycVerified,
                    walletBalance = balance ?: 0.0
                )
            }
        }
    }

    override suspend fun saveUser(user: User) {
        val existingEntity = dao.getUserById(user.id)
        val entity = UserEntity(
            id = user.id,
            phoneNumber = user.phoneNumber,
            fullName = user.fullName,
            role = user.role.name,
            kycVerified = user.kycVerified,
            kycReferenceToken = existingEntity?.kycReferenceToken,
            kycVerifiedName = existingEntity?.kycVerifiedName,
            completedBookings = existingEntity?.completedBookings ?: 0,
            avgHostRating = existingEntity?.avgHostRating ?: 5.0
        )
        dao.insertUser(entity)
    }

    override suspend fun completeProfile(user: User) {
        // 1. Persist full user entity to Room
        saveUser(user)

        // 2. Cache session flags locally via DataStore Preferences
        sessionDataStore.saveSessionFlags(
            userId = user.id,
            role = user.role.name,       // "GUEST" or "HOST"
            isProfileComplete = true
        )

        // 3. Create user profile row in Supabase (non-fatal while constants are placeholders)
        pushProfileToSupabase(user)
    }

    override suspend fun verifyKyc(userId: String, fullName: String, referenceToken: String): Boolean {
        val user = dao.getUserById(userId) ?: return false
        dao.insertUser(
            user.copy(
                kycVerified = true,
                kycVerifiedName = fullName,
                kycReferenceToken = referenceToken
            )
        )
        return true
    }

    // Reads Supabase config from DataStore (seeded from AppConstants) and POSTs the profile row.
    private suspend fun pushProfileToSupabase(user: User) {
        val supabaseUrl = sessionDataStore.getSupabaseUrl()
        val supabaseKey = sessionDataStore.getSupabaseAnonKey()

        val json = JSONObject().apply {
            put("id", user.id)
            put("phone_number", user.phoneNumber)
            put("full_name", user.fullName)
            put("role", user.role.name)
            put("kyc_verified", user.kycVerified)
            put("wallet_balance", user.walletBalance)
        }

        val requestBody = json.toString()
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$supabaseUrl/rest/v1/profiles")
            .addHeader("apikey", supabaseKey)
            .addHeader("Authorization", "Bearer $supabaseKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .post(requestBody)
            .build()

        try {
            withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute().use { response ->
                    // 409 Conflict = profile already exists — acceptable on retry
                    // Other non-2xx responses are non-fatal: Room + DataStore saves already succeeded
                    if (!response.isSuccessful && response.code != 409) {
                        // Will be retried on next completeProfile call
                    }
                }
            }
        } catch (_: Exception) {
            // Network unavailable or placeholder URL active — local-first guarantees app continues
        }
    }

    fun mapFirebaseUserToDomain(firebaseUser: FirebaseUser, desiredRole: UserRole = UserRole.GUEST): User {
        return User(
            id = firebaseUser.uid,
            phoneNumber = firebaseUser.phoneNumber ?: "",
            fullName = firebaseUser.displayName ?: "New Gusto User",
            role = desiredRole,
            kycVerified = false,
            walletBalance = 0.0
        )
    }
}
