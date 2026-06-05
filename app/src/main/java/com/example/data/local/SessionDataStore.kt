package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

class SessionDataStore(private val context: Context) {

    companion object {
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USER_ROLE = stringPreferencesKey("user_role")
        val KEY_PROFILE_COMPLETE = booleanPreferencesKey("profile_complete")
        val KEY_SUPABASE_URL = stringPreferencesKey("supabase_url")
        val KEY_SUPABASE_ANON_KEY = stringPreferencesKey("supabase_anon_key")
    }

    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val userRole: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ROLE] }
    val isProfileComplete: Flow<Boolean> = context.dataStore.data.map { it[KEY_PROFILE_COMPLETE] ?: false }

    suspend fun saveSessionFlags(userId: String, role: String, isProfileComplete: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_ROLE] = role
            prefs[KEY_PROFILE_COMPLETE] = isProfileComplete
        }
    }

    // Reads Supabase URL from DataStore; seeds from AppConstants on first call.
    suspend fun getSupabaseUrl(): String {
        return context.dataStore.data.map { it[KEY_SUPABASE_URL] }.first()
            ?: AppConstants.SUPABASE_URL.also { url ->
                context.dataStore.edit { it[KEY_SUPABASE_URL] = url }
            }
    }

    // Reads Supabase anon key from DataStore; seeds from AppConstants on first call.
    suspend fun getSupabaseAnonKey(): String {
        return context.dataStore.data.map { it[KEY_SUPABASE_ANON_KEY] }.first()
            ?: AppConstants.SUPABASE_ANON_KEY.also { key ->
                context.dataStore.edit { it[KEY_SUPABASE_ANON_KEY] = key }
            }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
