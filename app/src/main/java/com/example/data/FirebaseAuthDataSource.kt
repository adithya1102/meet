package com.example.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class FirebaseAuthDataSource {

    private val auth: FirebaseAuth? get() = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    val currentUserToken: String?
        get() = try { auth?.currentUser?.uid } catch (e: Exception) { null }

    fun isUserLoggedIn(): Boolean {
        return try { auth?.currentUser != null } catch (e: Exception) { false }
    }

    fun signOut() {
        try { auth?.signOut() } catch (e: Exception) {}
    }

    sealed class PhoneAuthResult {
        data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : PhoneAuthResult()
        data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneAuthResult()
        data class VerificationFailed(val exception: FirebaseException) : PhoneAuthResult()
    }

    fun sendOtp(phoneNumber: String, activity: Activity): Flow<PhoneAuthResult> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            trySend(PhoneAuthResult.VerificationFailed(com.google.firebase.FirebaseApiNotAvailableException("Firebase Auth is unavailable or not initialized")))
            close()
            return@callbackFlow
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(PhoneAuthResult.VerificationCompleted(credential))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                trySend(PhoneAuthResult.VerificationFailed(e))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(PhoneAuthResult.CodeSent(verificationId, token))
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        awaitClose { /* Cleanup if necessary */ }
    }

    suspend fun verifyOtpCredential(credential: PhoneAuthCredential): AuthResult {
        val firebaseAuth = auth ?: throw Exception("Firebase Auth is not available")
        val result = firebaseAuth.signInWithCredential(credential).await()
        if (result.user == null) {
            throw Exception("Firebase Auth result contains no user")
        }
        return result
    }

    suspend fun signInWithGoogle(idToken: String): AuthResult {
        val firebaseAuth = auth ?: throw Exception("Firebase Auth is not available")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return firebaseAuth.signInWithCredential(credential).await()
    }
}
