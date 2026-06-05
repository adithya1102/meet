package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(userId: String): Flow<User?>
    suspend fun saveUser(user: User)
    // Saves user locally (Room + DataStore session flags) and creates the profile row in Supabase.
    suspend fun completeProfile(user: User)
    suspend fun verifyKyc(userId: String, fullName: String, referenceToken: String): Boolean
}
