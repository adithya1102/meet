package com.example.data.remote

import kotlinx.coroutines.delay
import java.util.UUID

class MockKycDataSource : KycApiService {
    override suspend fun verifyAadhaar(request: AadhaarVerifyRequest): AadhaarVerifyResponse {
        delay(2000)
        val randomToken = "setu_ref_" + UUID.randomUUID().toString().replace("-", "").take(16)
        return AadhaarVerifyResponse(
            verified = true,
            verifiedName = request.fullName.trim().uppercase(),
            referenceToken = randomToken
        )
    }
}
