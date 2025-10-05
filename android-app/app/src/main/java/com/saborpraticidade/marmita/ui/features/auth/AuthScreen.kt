package com.saborpraticidade.marmita.ui.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    state: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onFullNameChange: (String) -> Unit = {},
    onPhoneChange: (String) -> Unit = {},
    onCepChange: (String) -> Unit = {},
    onStreetChange: (String) -> Unit = {},
    onNumberChange: (String) -> Unit = {},
    onComplementChange: (String) -> Unit = {},
    onNeighborhoodChange: (String) -> Unit = {},
    onCityChange: (String) -> Unit = {},
    onStateUfChange: (String) -> Unit = {},
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    val isSignUp = remember { mutableStateOf(true) }
    Scaffold(topBar = { TopAppBar(title = { Text(if (isSignUp.value) "Cadastrar" else "Entrar") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = { isSignUp.value = !isSignUp.value },
                label = { Text(if (isSignUp.value) "Já tenho conta" else "Quero me cadastrar") }
            )
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            if (isSignUp.value) {
                OutlinedTextField(
                    value = state.fullName,
                    onValueChange = onFullNameChange,
                    label = { Text("Nome completo (obrigatório)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = onPhoneChange,
                    label = { Text("Telefone (obrigatório)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Divider()
                Text("Endereço para entrega", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = state.cep,
                    onValueChange = onCepChange,
                    label = { Text("CEP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.street,
                    onValueChange = onStreetChange,
                    label = { Text("Logradouro (rua/avenida)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.number,
                    onValueChange = onNumberChange,
                    label = { Text("Número") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = state.complement,
                    onValueChange = onComplementChange,
                    label = { Text("Complemento (opcional)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.neighborhood,
                    onValueChange = onNeighborhoodChange,
                    label = { Text("Bairro") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.city,
                    onValueChange = onCityChange,
                    label = { Text("Cidade") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.stateUf,
                    onValueChange = onStateUfChange,
                    label = { Text("UF") },
                    singleLine = true
                )
            }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (isSignUp.value) {
                Button(onClick = onSignUp, enabled = !state.loading) { Text("Cadastrar") }
            } else {
                Button(onClick = onSignIn, enabled = !state.loading) { Text("Entrar") }
            }
        }
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val phone: String = "",
    val cep: String = "",
    val street: String = "",
    val number: String = "",
    val complement: String = "",
    val neighborhood: String = "",
    val city: String = "",
    val stateUf: String = "",
    val loading: Boolean = false,
    val error: String? = null
)