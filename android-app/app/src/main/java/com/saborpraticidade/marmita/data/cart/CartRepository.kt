package com.saborpraticidade.marmita.data.cart

import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CartRepository {
    private val _items = MutableStateFlow<Map<String, OrderItem>>(emptyMap())
    val items: StateFlow<Map<String, OrderItem>> = _items

    fun add(m: Marmita) {
        val id = m.id ?: return
        val current = _items.value.toMutableMap()
        val existing = current[id]
        if (existing == null) {
            current[id] = OrderItem(id = id, name = m.name, price = m.price, quantity = 1)
        } else {
            current[id] = existing.copy(quantity = existing.quantity + 1)
        }
        _items.value = current
    }

    fun update(id: String, quantity: Int) {
        val current = _items.value.toMutableMap()
        if (quantity <= 0) current.remove(id) else current[id]?.let { current[id] = it.copy(quantity = quantity) }
        _items.value = current
    }

    fun remove(id: String) {
        val current = _items.value.toMutableMap()
        current.remove(id)
        _items.value = current
    }

    fun clear() { _items.value = emptyMap() }

    fun total(): Double = _items.value.values.sumOf { it.price * it.quantity }
}
