package com.saborpraticidade.marmita.ui.features.order

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(onDone: () -> Unit, vm: CheckoutViewModel = getViewModel()) {
    val state by vm.uiState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Finalizar Pedido") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = state.name, onValueChange = vm::setName, label = { Text("Nome completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = state.phone,
                onValueChange = vm::setPhoneMasked,
                isError = state.phoneError != null,
                label = { Text("Telefone") },
                supportingText = { state.phoneError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = state.address, onValueChange = vm::setAddress, label = { Text("Endereço") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.notes, onValueChange = vm::setNotes, label = { Text("Observações (opcional)") }, modifier = Modifier.fillMaxWidth())

            // Pagamento
            Text("Forma de pagamento", style = MaterialTheme.typography.titleSmall)
            val methods = listOf("pix", "dinheiro", "cartao", "mock")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                methods.forEach { m ->
                    FilterChip(
                        selected = state.paymentMethod == m,
                        onClick = { vm.setPaymentMethod(m) },
                        label = { Text(m.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            // Resumo do carrinho
            if (state.items.isNotEmpty()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Itens do pedido", style = MaterialTheme.typography.titleMedium)
                        state.items.forEach { item ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.name} x${item.quantity}")
                                Text("R$ %.2f".format(item.price * item.quantity))
                            }
                        }
                        Divider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total:", style = MaterialTheme.typography.titleMedium)
                            Text("R$ %.2f".format(state.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total:", style = MaterialTheme.typography.titleMedium)
                    Text("R$ %.2f".format(state.total), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
            }

            Button(onClick = { vm.submit(onDone) }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.loading) "Processando..." else "Confirmar Pedido")
            }

            if (state.error != null) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
