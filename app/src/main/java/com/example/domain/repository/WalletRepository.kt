package com.example.domain.repository

import com.example.domain.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getWalletBalance(userId: String): Flow<Double>
    fun getTransactions(userId: String): Flow<List<WalletTransaction>>
    suspend fun addTransaction(transaction: WalletTransaction)
}
