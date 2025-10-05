package com.saborpraticidade.marmita.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("session")
private val KEY_TOKEN = stringPreferencesKey("access_token")

class SessionManager(private val context: Context) {
    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[KEY_TOKEN] = token }
    }
    suspend fun getToken(): String? = context.dataStore.data.map { it[KEY_TOKEN] }.first()
    suspend fun clear() { context.dataStore.edit { it.remove(KEY_TOKEN) } }
}