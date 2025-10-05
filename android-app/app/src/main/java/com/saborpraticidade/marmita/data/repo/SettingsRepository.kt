package com.saborpraticidade.marmita.data.repo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.saborpraticidade.marmita.data.session.SessionManager
import com.saborpraticidade.marmita.data.Setting

class SettingsRepository(
    private val http: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val session: SessionManager
) {
    private fun postgrest(path: String) = if (path.startsWith("/")) "$supabaseUrl/rest/v1$path" else "$supabaseUrl/rest/v1/$path"

    private fun anonHeaders(builder: HttpRequestBuilder) {
        builder.headers {
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $supabaseKey")
            append("Accept-Profile", "public")
            append("Content-Profile", "public")
            append("Prefer", "return=representation")
        }
    }

    private suspend fun authHeaders(builder: HttpRequestBuilder) {
        val token = session.getToken() ?: ""
        builder.headers {
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $token")
            append("Accept-Profile", "public")
            append("Content-Profile", "public")
            append("Prefer", "return=representation")
        }
    }

    suspend fun getBannerUrl(): String? {
        val url = postgrest("settings?select=key,value&key=eq.main_banner_url&limit=1")
        val list: List<Setting> = http.get(url) { anonHeaders(this) }.body()
        return list.firstOrNull()?.value
    }

    suspend fun getLogoUrl(): String? {
        val url = postgrest("settings?select=key,value&key=eq.main_logo_url&limit=1")
        val list: List<Setting> = http.get(url) { anonHeaders(this) }.body()
        return list.firstOrNull()?.value
    }

    suspend fun setLogoUrl(urlValue: String) {
        val url = postgrest("settings?on_conflict=key")
        http.post(url) {
            authHeaders(this)
            headers { append("Prefer", "resolution=merge-duplicates") }
            contentType(ContentType.Application.Json)
            setBody(listOf(Setting(key = "main_logo_url", value = urlValue)))
        }.body<List<Setting>>()
    }

    suspend fun getLogoSizeDp(): Int? {
        val url = postgrest("settings?select=key,value&key=eq.main_logo_size_dp&limit=1")
        val list: List<Setting> = http.get(url) { anonHeaders(this) }.body()
        val v = list.firstOrNull()?.value ?: return null
        return v.toIntOrNull()
    }

    suspend fun setLogoSizeDp(value: Int) {
        val url = postgrest("settings?on_conflict=key")
        http.post(url) {
            authHeaders(this)
            headers { append("Prefer", "resolution=merge-duplicates") }
            contentType(ContentType.Application.Json)
            setBody(listOf(Setting(key = "main_logo_size_dp", value = value.toString())))
        }.body<List<Setting>>()
    }

    suspend fun getStoreName(): String? {
        val url = postgrest("settings?select=key,value&key=eq.store_name&limit=1")
        val list: List<Setting> = http.get(url) { anonHeaders(this) }.body()
        return list.firstOrNull()?.value
    }

    suspend fun setStoreName(name: String) {
        val url = postgrest("settings?on_conflict=key")
        http.post(url) {
            authHeaders(this)
            headers { append("Prefer", "resolution=merge-duplicates") }
            contentType(ContentType.Application.Json)
            setBody(listOf(Setting(key = "store_name", value = name)))
        }.body<List<Setting>>()
    }

    suspend fun setBannerUrl(urlValue: String) {
        val url = postgrest("settings")
        http.post(url) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(listOf(Setting(key = "main_banner_url", value = urlValue)))
        }.body<List<Setting>>()
    }
}
