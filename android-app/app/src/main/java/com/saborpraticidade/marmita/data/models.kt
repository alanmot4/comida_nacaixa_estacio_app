package com.saborpraticidade.marmita.data

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String? = null,
    val name: String,
)

@Serializable
data class Marmita(
    val id: String? = null,
    val name: String,
    val description: String? = null,
    val price: Double,
    val image_url: String? = null,
    val category_id: String? = null,
    val available: Boolean? = true,
    val ingredients: List<MarmitaIngredient> = emptyList(),
)

@Serializable
data class MarmitaIngredient(
    val name: String,
    val grams: Int,
)

@Serializable
data class OrderItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
)

@Serializable
data class Order(
    val id: String? = null,
    val customer_name: String,
    val customer_phone: String,
    val customer_address: String,
    val items: List<OrderItem>,
    val total: Double,
    val status: String? = "pending",
    val notes: String? = null,
    val priority: Int = 0,
    val created_at: String? = null,
)

@Serializable
data class Inventory(
    val id: String? = null,
    val marmita_id: String,
    val quantity: Int,
)

@Serializable
data class Payment(
    val id: String? = null,
    val order_id: String,
    val method: String, // 'pix', 'dinheiro', 'cartao', 'mock'
    val amount: Double,
    val status: String = "pending",
)

@Serializable
data class Profile(
    val id: String,
    val full_name: String? = null,
    val phone: String? = null,
)

@Serializable
data class Setting(
    val key: String,
    val value: String? = null,
)
