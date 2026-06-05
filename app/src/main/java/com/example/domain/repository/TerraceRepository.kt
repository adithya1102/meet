package com.example.domain.repository

import com.example.domain.model.Terrace
import com.example.domain.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

interface TerraceRepository {
    fun getTerrace(terraceId: String): Flow<Terrace?>
    fun getAllTerraces(): Flow<List<Terrace>>
    fun getTerracesByHost(hostId: String): Flow<List<Terrace>>
    suspend fun saveTerrace(terrace: Terrace)
    suspend fun updateVerificationStatus(terraceId: String, status: VerificationStatus)
}
