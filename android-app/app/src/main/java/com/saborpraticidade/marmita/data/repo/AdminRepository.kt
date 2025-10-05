package com.saborpraticidade.marmita.data.repo

import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.Order
import com.saborpraticidade.marmita.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AdminRepository(
    private val http: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val session: SessionManager
) {
    private fun postgrest(path: String) = if (path.startsWith("/")) "$supabaseUrl/rest/v1$path" else "$supabaseUrl/rest/v1/$path"

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

    suspend fun listAllMarmitas(): List<Marmita> {
        val url = postgrest("marmitas?select=*&order=created_at.desc")
        return http.get(url) { authHeaders(this) }.body()
    }

    suspend fun createMarmita(m: Marmita): Marmita {
        val url = postgrest("marmitas")
        return http.post(url) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(m)
        }.body()
    }

    suspend fun updateMarmita(id: String, m: Marmita): Marmita {
        val url = postgrest("marmitas?id=eq.$id")
        return http.patch(url) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(m)
        }.body()
    }

    suspend fun deleteMarmita(id: String) {
        val url = postgrest("marmitas?id=eq.$id")
        http.delete(url) { authHeaders(this) }
    }

    // Orders admin
    suspend fun listOrders(): List<Order> {
        val url = postgrest("orders?select=*&order=priority.desc,created_at.asc")
        return http.get(url) { authHeaders(this) }.body()
    }

    suspend fun updateOrderStatus(id: String, status: String) {
        val url = postgrest("orders?id=eq.$id")
        http.patch(url) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(mapOf("status" to status))
        }.body<Unit>()
    }

    suspend fun updateOrderPriority(id: String, priority: Int) {
        val url = postgrest("orders?id=eq.$id")
        http.patch(url) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(mapOf("priority" to priority))
        }.body<Unit>()
    }
}
