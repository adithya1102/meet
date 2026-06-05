package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.remote.AadhaarVerifyRequest
import com.example.data.remote.KycApiService
import com.example.domain.model.OwnershipType
import com.example.domain.repository.UserRepository
import com.example.lib.utils.VerhoeffValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kyc_prefs")

sealed class KycUiState {
    object Idle : KycUiState()
    object ValidatingLocally : KycUiState()
    object Submitting : KycUiState()
    data class Success(val verifiedName: String) : KycUiState()
    data class Error(val message: String) : KycUiState()
}

class KycViewModel(
    application: Application,
    private val userRepository: UserRepository,
    private val kycApiService: KycApiService
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<KycUiState>(KycUiState.Idle)
    val uiState: StateFlow<KycUiState> = _uiState.asStateFlow()

    private val _aadhaarValid = MutableStateFlow<Boolean?>(null)
    val aadhaarValid: StateFlow<Boolean?> = _aadhaarValid.asStateFlow()

    companion object {
        val KYC_VERIFIED_KEY = booleanPreferencesKey("kyc_verified")

        fun provideFactory(application: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return KycViewModel(
                    application,
                    com.example.di.ServiceLocator.userRepository,
                    com.example.di.ServiceLocator.kycApiService
                ) as T
            }
        }
    }

    fun onAadhaarChanged(input: String) {
        if (input.length < 12) {
            _aadhaarValid.value = null
            return
        }
        _uiState.value = KycUiState.ValidatingLocally
        val isValid = VerhoeffValidator.validate(input)
        _aadhaarValid.value = isValid
        _uiState.value = KycUiState.Idle
    }

    fun submitKyc(
        aadhaarNumber: String,
        fullName: String,
        ownershipType: OwnershipType?,
        nocUri: String?,
        userId: String,
        userRole: String
    ) {
        if (_aadhaarValid.value != true) {
            _uiState.value = KycUiState.Error("Invalid Aadhaar checksum calculation. Incorrect number pattern.")
            return
        }

        _uiState.value = KycUiState.Submitting
        viewModelScope.launch {
            try {
                val request = AadhaarVerifyRequest(
                    aadhaarNumber = aadhaarNumber,
                    fullName = fullName,
                    role = userRole,
                    ownershipType = ownershipType?.name,
                    nocUri = nocUri
                )
                val response = kycApiService.verifyAadhaar(request)

                if (response.verified) {
                    val dbSuccess = userRepository.verifyKyc(
                        userId = userId,
                        fullName = response.verifiedName,
                        referenceToken = response.referenceToken
                    )

                    if (dbSuccess) {
                        getApplication<Application>().dataStore.edit { preferences ->
                            preferences[KYC_VERIFIED_KEY] = true
                        }
                        _uiState.value = KycUiState.Success(response.verifiedName)
                    } else {
                        _uiState.value = KycUiState.Error("Supabase update failure. User record does not exist locally.")
                    }
                } else {
                    _uiState.value = KycUiState.Error("Aadhaar demographic checking failed.")
                }
            } catch (e: Exception) {
                _uiState.value = KycUiState.Error(e.localizedMessage ?: "Critical KYC transmission error occurred.")
            }
        }
    }

    fun resetState() {
        _uiState.value = KycUiState.Idle
        _aadhaarValid.value = null
    }
}
