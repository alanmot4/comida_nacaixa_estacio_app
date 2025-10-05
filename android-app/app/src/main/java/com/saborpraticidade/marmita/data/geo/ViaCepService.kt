package com.saborpraticidade.marmita.data.geo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ViaCepResponse(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val bairro: String? = null,
    val localidade: String? = null,
    val uf: String? = null,
    val ibge: String? = null,
    val gia: String? = null,
    val ddd: String? = null,
    val siafi: String? = null,
    @SerialName("erro") val error: Boolean? = null,
)

class ViaCepService(private val http: HttpClient) {
    suspend fun lookup(cepDigitsOnly: String): ViaCepResponse? {
        if (cepDigitsOnly.length != 8) return null
        val url = "https://viacep.com.br/ws/${cepDigitsOnly}/json/"
        val res: ViaCepResponse = http.get(url).body()
        if (res.error == true) return null
        return res
    }
}
