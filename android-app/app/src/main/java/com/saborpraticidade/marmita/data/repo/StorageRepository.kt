package com.saborpraticidade.marmita.data.repo

import com.saborpraticidade.marmita.data.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.UUID

class StorageRepository(
    private val http: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val session: SessionManager
) {
    private fun storageObjectUrl(bucket: String, path: String) = "$supabaseUrl/storage/v1/object/$bucket/$path"
    private fun publicUrl(bucket: String, path: String) = "$supabaseUrl/storage/v1/object/public/$bucket/$path"

    suspend fun uploadImageToMarmitas(bytes: ByteArray, mime: String): String {
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            else -> "jpg"
        }
        val fileName = UUID.randomUUID().toString() + "." + ext
        val bucket = "marmitas"
        val path = fileName
        val token = session.getToken() ?: ""
        val url = storageObjectUrl(bucket, path)
        http.post(url) {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $token")
                append("x-upsert", "true")
            }
            contentType(ContentType.parse(mime))
            setBody(bytes)
        }.body<Unit>()
        return publicUrl(bucket, path)
    }

    suspend fun uploadBanner(bytes: ByteArray, mime: String): String {
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            else -> "jpg"
        }
        val fileName = UUID.randomUUID().toString() + "." + ext
        val bucket = "banners"
        val path = fileName
        val token = session.getToken() ?: ""
        val url = storageObjectUrl(bucket, path)
        http.post(url) {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $token")
                append("x-upsert", "true")
            }
            contentType(ContentType.parse(mime))
            setBody(bytes)
        }.body<Unit>()
        return publicUrl(bucket, path)
    }

    suspend fun uploadLogo(bytes: ByteArray, mime: String): String {
        val ext = when (mime.lowercase()) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic" -> "heic"
            else -> "jpg"
        }
        val fileName = UUID.randomUUID().toString() + "." + ext
        val bucket = "logos"
        val path = fileName
        val token = session.getToken() ?: ""
        val url = storageObjectUrl(bucket, path)
        http.post(url) {
            headers {
                append("apikey", supabaseKey)
                append("Authorization", "Bearer $token")
                append("x-upsert", "true")
            }
            contentType(ContentType.parse(mime))
            setBody(bytes)
        }.body<Unit>()
        return publicUrl(bucket, path)
    }
}
