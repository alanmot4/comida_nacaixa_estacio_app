package com.saborpraticidade.marmita.data.auth

import com.saborpraticidade.marmita.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TokenResponse(val access_token: String? = null)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>
)

class AuthRepository(
    private val client: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val session: SessionManager
) {
    suspend fun signIn(email: String, password: String) {
        try {
            val res: TokenResponse = client.post("$supabaseUrl/auth/v1/token?grant_type=password") {
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer $supabaseKey")
                }
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to email, "password" to password))
            }.body()
            val token = res.access_token
            if (token.isNullOrBlank()) {
                throw IllegalStateException("Login falhou: verifique e-mail/senha ou confirme seu e-mail.")
            }
            session.saveToken(token)
        } catch (e: ClientRequestException) {
            throw IllegalStateException("Login inválido: verifique e-mail/senha ou confirme seu e-mail.")
        } catch (e: ServerResponseException) {
            throw IllegalStateException("Servidor indisponível no momento. Tente novamente.")
        }
    }

    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        address: Map<String, String>? = null
    ) {
        client.post("$supabaseUrl/auth/v1/signup") {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $supabaseKey")
            }
            contentType(ContentType.Application.Json)
            val data = mutableMapOf(
                "full_name" to fullName,
                "phone" to phone
            )
            // Include address fields (only non-empty)
            address?.forEach { (k, v) -> if (!v.isNullOrBlank()) data[k] = v }
            // Use a typed serializable request to avoid Map<String, Any> heterogeneity
            setBody(SignUpRequest(email = email, password = password, data = data))
        }
        // Se o projeto exigir confirmação por e-mail, o usuário precisará confirmar antes de fazer login.
    }
}