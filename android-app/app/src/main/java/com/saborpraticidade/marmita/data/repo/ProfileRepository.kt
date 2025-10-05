package com.saborpraticidade.marmita.data.repo

import com.saborpraticidade.marmita.data.Profile
import com.saborpraticidade.marmita.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileRepository(
    private val http: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val session: SessionManager
) {
    private fun postgrest(path: String) = if (path.startsWith("/")) "$supabaseUrl/rest/v1$path" else "$supabaseUrl/rest/v1/$path"

    private suspend fun authHeaders(builder: io.ktor.client.request.HttpRequestBuilder) {
        val token = session.getToken() ?: ""
        builder.headers {
            append("apikey", supabaseKey)
            append("Authorization", "Bearer $token")
            append("Accept-Profile", "public")
            append("Content-Profile", "public")
            append("Prefer", "return=representation,resolution=merge-duplicates")
        }
    }

    private suspend fun currentUserId(): String? {
        val obj = getAuthObject() ?: return null
        val idTop = obj["id"]?.jsonPrimitive?.let { if (it.isString) it.content else null }
        if (!idTop.isNullOrBlank()) return idTop
        val idNested = obj["user"]?.jsonObject?.get("id")?.jsonPrimitive?.let { if (it.isString) it.content else null }
        return idNested
    }

    suspend fun currentUserEmail(): String? {
        val obj = getAuthObject() ?: return null
        val eTop = obj["email"]?.jsonPrimitive?.let { if (it.isString) it.content else null }
        if (!eTop.isNullOrBlank()) return eTop
        val eNested = obj["user"]?.jsonObject?.get("email")?.jsonPrimitive?.let { if (it.isString) it.content else null }
        return eNested
    }

    private suspend fun getAuthObject(): JsonObject? {
        val token = session.getToken() ?: return null
        val resp: HttpResponse = http.get("$supabaseUrl/auth/v1/user") {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $token")
            }
        }
        if (resp.status != HttpStatusCode.OK) return null
        val text = resp.bodyAsText()
        val el = runCatching { Json.parseToJsonElement(text) }.getOrNull() ?: return null
        val obj = el.jsonObject
        // Normalize to inner object when response is wrapped in { "user": { ... } }
        return obj["user"]?.jsonObject ?: obj
    }

    data class Address(
        val cep: String? = null,
        val street: String? = null,
        val number: String? = null,
        val complement: String? = null,
        val neighborhood: String? = null,
        val city: String? = null,
        val state: String? = null,
    )

    suspend fun currentUserAddress(): Address? {
        val obj = getAuthObject() ?: return null
        val meta = obj["user_metadata"]?.jsonObject ?: return null
        fun metaString(key: String): String? = meta[key]?.jsonPrimitive?.let { if (it.isString) it.content else null }
        return Address(
            cep = metaString("cep"),
            street = metaString("street"),
            number = metaString("number"),
            complement = metaString("complement"),
            neighborhood = metaString("neighborhood"),
            city = metaString("city"),
            state = metaString("state"),
        )
    }

    data class BasicInfo(val fullName: String? = null, val phone: String? = null)

    suspend fun currentUserBasicInfo(): BasicInfo? {
        val obj = getAuthObject() ?: return null
        val meta = obj["user_metadata"]?.jsonObject ?: return null
        fun metaString(key: String): String? = meta[key]?.jsonPrimitive?.let { if (it.isString) it.content else null }
        return BasicInfo(
            fullName = metaString("full_name"),
            phone = metaString("phone"),
        )
    }

    suspend fun getMyProfile(): Profile? {
        val uid = currentUserId() ?: throw IllegalStateException("Sessão expirada ou inválida. Faça login novamente.")
        val url = postgrest("profiles?select=*&id=eq.$uid&limit=1")
        val list: List<Profile> = http.get(url) { authHeaders(this) }.body()
        return list.firstOrNull()
    }

    @Serializable
    private data class RoleRow(val role: String)

    suspend fun getMyRoles(): List<String> {
        val uid = currentUserId() ?: return emptyList()
        val url = postgrest("user_roles?select=role&user_id=eq.$uid")
        val list: List<RoleRow> = http.get(url) { authHeaders(this) }.body()
        return list.map { it.role }
    }

    suspend fun upsertMyProfile(fullName: String?, phone: String?): Profile {
        val fields = mapOf(
            "full_name" to fullName,
            "phone" to phone
        )

    val uid = currentUserId() ?: throw IllegalStateException("Sessão expirada ou inválida. Faça login novamente.")
        // Try update first by id
        val updateUrl = postgrest("profiles?id=eq.$uid")
        var res: List<Profile> = http.patch(updateUrl) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(fields)
        }.body()
        if (res.isNotEmpty()) return res.first()

        // If nothing updated, insert row with explicit id
        val insertUrl = postgrest("profiles")
        res = http.post(insertUrl) {
            authHeaders(this)
            contentType(ContentType.Application.Json)
            setBody(fields + mapOf("id" to uid))
        }.body()
        // If insert didn't return, fallback to select
        if (res.isNotEmpty()) return res.first()
        return getMyProfile() ?: Profile(id = uid, full_name = fields["full_name"] as String?, phone = fields["phone"] as String?)
    }
}