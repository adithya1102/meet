package com.example.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class AadhaarVerifyRequest(
    val aadhaarNumber: String,
    val fullName: String,
    val role: String,
    val ownershipType: String? = null,
    val nocUri: String? = null
)

data class AadhaarVerifyResponse(
    val verified: Boolean,
    val verifiedName: String,
    val referenceToken: String
)

interface KycApiService {
    @POST("/api/v1/kyc/verify")
    suspend fun verifyAadhaar(
        @Body request: AadhaarVerifyRequest
    ): AadhaarVerifyResponse
}
