package com.saborpraticidade.marmita.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

data class ProfileUiState(
    val fullName: String = "",
    val phone: String = "",
    val roles: List<String> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val email: String? = null,
    val addressLine1: String? = null,
    val addressLine2: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    onOpenAdminOrders: () -> Unit = {},
    onOpenCustomize: () -> Unit = {},
    onOpenMyOrders: () -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar("Dados salvos com sucesso")
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Seu perfil") }) }, snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            // Header card with avatar and email
            ElevatedCard(Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.elevatedCardColors()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Informações da conta", style = MaterialTheme.typography.titleMedium)
                    if (!state.email.isNullOrBlank()) {
                        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Customer actions
            ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Pedidos", style = MaterialTheme.typography.titleMedium)
                    Text("Veja seu histórico de pedidos e status.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = onOpenMyOrders, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) { Text("Meus pedidos") }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Address summary
            if (!state.addressLine1.isNullOrBlank() || !state.addressLine2.isNullOrBlank()) {
                ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Endereço", style = MaterialTheme.typography.titleMedium)
                        state.addressLine1?.let { if (it.isNotBlank()) Text(it) }
                        state.addressLine2?.let { if (it.isNotBlank()) Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Editable fields
            ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = state.fullName, onValueChange = onFullNameChange, label = { Text("Nome completo") }, placeholder = { Text("Seu nome") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = state.phone, onValueChange = onPhoneChange, label = { Text("Telefone") }, placeholder = { Text("(xx) xxxxx-xxxx") }, modifier = Modifier.fillMaxWidth())
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            }

            // Roles and admin actions
            if (state.roles.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Suas permissões", style = MaterialTheme.typography.titleMedium)
                    Text(state.roles.joinToString(", ") { it.replaceFirstChar { c -> c.titlecase() } }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val isAdmin = remember(state.roles) { state.roles.any { it.equals("admin", ignoreCase = true) } }
            if (isAdmin) {
                ElevatedCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.elevatedCardColors()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ações de administrador", style = MaterialTheme.typography.titleMedium)
                        Text("Abra as telas dedicadas para gerenciar marmitas, pedidos e personalizar a loja.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = onOpenAdmin, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) { Text("Gerenciar marmitas") }
                        OutlinedButton(onClick = onOpenAdminOrders, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) { Text("Gerenciar pedidos") }
                        OutlinedButton(onClick = onOpenCustomize, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) { Text("Personalizar loja") }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Spacer(Modifier.height(120.dp))
        }

        // Sticky save bar at bottom
        Box(Modifier.fillMaxSize()) {
            Surface(shadowElevation = 8.dp, tonalElevation = 8.dp, modifier = Modifier.align(Alignment.BottomCenter)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onSave, enabled = !state.loading, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) {
                        Text(if (state.loading) "Salvando..." else "Salvar")
                    }
                }
            }
        }
    }
}