package com.saborpraticidade.marmita.ui.features.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(onBack: () -> Unit, vm: MyOrdersViewModel = getViewModel()) {
    val state by vm.ui.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Meus pedidos") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (!state.loading && state.orders.isEmpty()) {
                Text("Você ainda não tem pedidos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.orders, key = { it.id ?: it.created_at ?: it.customer_phone }) { order ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("#${order.id ?: "s/ id"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Total: R$ %.2f".format(order.total), style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${order.status ?: "pendente"}")
                            order.created_at?.let { Text("Criado em: $it", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Text(order.customer_address, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
