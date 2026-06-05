package com.example.di

import android.content.Context
import com.example.data.FirebaseAuthDataSource
import com.example.data.local.AppDatabase
import com.example.data.local.SessionDataStore
import com.example.data.local.dao.MeetsDao
import com.example.data.remote.KycApiService
import com.example.data.remote.MockKycDataSource
import com.example.data.repository.UserRepositoryImpl
import com.example.domain.repository.UserRepository
import okhttp3.OkHttpClient

object ServiceLocator {
    private var context: Context? = null

    fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    private fun requireContext(): Context =
        context ?: throw IllegalStateException("ServiceLocator has not been initialized with Context")

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(requireContext())
    }

    val meetsDao: MeetsDao by lazy {
        database.meetsDao()
    }

    val sessionDataStore: SessionDataStore by lazy {
        SessionDataStore(requireContext())
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    val userRepository: UserRepository by lazy {
        UserRepositoryImpl(meetsDao, sessionDataStore, okHttpClient)
    }

    val firebaseAuthDataSource: FirebaseAuthDataSource by lazy {
        FirebaseAuthDataSource()
    }

    val kycApiService: KycApiService by lazy { MockKycDataSource() }
}
