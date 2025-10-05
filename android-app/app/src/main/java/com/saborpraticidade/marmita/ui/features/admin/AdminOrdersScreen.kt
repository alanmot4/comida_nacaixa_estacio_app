@file:OptIn(ExperimentalMaterial3Api::class)
package com.saborpraticidade.marmita.ui.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saborpraticidade.marmita.data.Order
import com.saborpraticidade.marmita.data.repo.AdminRepository
import org.koin.compose.koinInject
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AdminOrdersScreen(onBack: () -> Unit = {}) {
    val repo: AdminRepository = koinInject()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        loading = true; error = null
        runCatching { repo.listOrders() }
            .onSuccess { orders = it }
            .onFailure { error = it.message }
        loading = false
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(topBar = { TopAppBar(title = { Text("Pedidos") }) }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(orders, key = { it.id ?: it.customer_phone }) { order ->
                    AdminOrderItem(order,
                        onPriority = { p ->
                            order.id?.let { id ->
                                scope.launch {
                                    runCatching { repo.updateOrderPriority(id, p) }
                                        .onSuccess { refresh() }
                                        .onFailure { error = it.message }
                                }
                            }
                        },
                        onStatus = { status ->
                            order.id?.let { id ->
                                scope.launch {
                                    runCatching { repo.updateOrderStatus(id, status) }
                                        .onSuccess { refresh() }
                                        .onFailure { error = it.message }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminOrderItem(order: Order, onPriority: (Int) -> Unit, onStatus: (String) -> Unit) {
    var p by remember(order.id) { mutableStateOf(order.priority) }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Cliente: ${order.customer_name}", style = MaterialTheme.typography.titleMedium)
            Text("Telefone: ${order.customer_phone}")
            Text("Endereço: ${order.customer_address}")
            Text("Total: R$ %.2f".format(order.total))
            Text("Status: ${order.status}")
            Text("Prioridade: $p")
            Slider(value = p.toFloat(), onValueChange = { p = it.toInt() }, valueRange = 0f..10f, steps = 9)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onPriority(p) }) { Text("Aplicar prioridade") }
                OutlinedButton(onClick = { onStatus("cancelled") }) { Text("Cancelar") }
                OutlinedButton(onClick = { onStatus("confirmed") }) { Text("Confirmar") }
                OutlinedButton(onClick = { onStatus("preparing") }) { Text("Preparar") }
                OutlinedButton(onClick = { onStatus("delivered") }) { Text("Entregar") }
            }
        }
    }
}
