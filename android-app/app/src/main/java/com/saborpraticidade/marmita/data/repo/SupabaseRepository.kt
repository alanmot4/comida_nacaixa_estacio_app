package com.saborpraticidade.marmita.data.repo

import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.Order
import com.saborpraticidade.marmita.data.Payment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.headers
import io.ktor.http.ContentType
import io.ktor.http.contentType

class SupabaseRepository(
    private val http: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String
) {
    private fun postgrest(path: String) =
        if (path.startsWith("/")) "$supabaseUrl/rest/v1$path" else "$supabaseUrl/rest/v1/$path"

    private fun commonHeaders(builder: io.ktor.client.request.HttpRequestBuilder) {
        builder.headers {
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $supabaseKey")
            append("Accept-Profile", "public")
            append("Content-Profile", "public")
            append("Prefer", "return=representation")
        }
    }

    suspend fun listMarmitas(): List<Marmita> {
        val url = postgrest("marmitas?select=*&available=eq.true&order=name")
        val resp = http.get(url) { commonHeaders(this) }
        return resp.body()
    }

    suspend fun listMarmitasPage(limit: Int, offset: Int): List<Marmita> {
        val url = postgrest("marmitas?select=*&available=eq.true&order=name&limit=$limit&offset=$offset")
        val resp = http.get(url) { commonHeaders(this) }
        return resp.body()
    }

    suspend fun getMarmita(id: String): Marmita? {
        val url = postgrest("marmitas?select=*&id=eq.$id&limit=1")
        val resp = http.get(url) { commonHeaders(this) }
        val list: List<Marmita> = resp.body()
        return list.firstOrNull()
    }

    suspend fun createOrder(order: Order): Order {
        val url = postgrest("orders")
        val resp = http.post(url) {
            commonHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(order)
        }
        return resp.body()
    }

    suspend fun createPayment(payment: Payment): Payment {
        val url = postgrest("payments")
        val resp = http.post(url) {
            commonHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(payment)
        }
        return resp.body()
    }

    // Customer: list my orders history
    suspend fun listMyOrders(phoneDigits: String?): List<Order> {
        val digits = phoneDigits?.filter { it.isDigit() }
        if (digits.isNullOrBlank()) return emptyList()
        val url = postgrest("orders?customer_phone=eq.$digits&select=*&order=created_at.desc")
        val resp = http.get(url) { commonHeaders(this) }
        return resp.body()
    }
}
