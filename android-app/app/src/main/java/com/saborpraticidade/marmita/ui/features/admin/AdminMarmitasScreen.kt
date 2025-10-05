package com.saborpraticidade.marmita.ui.features.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saborpraticidade.marmita.ui.features.profile.AdminMarmitasSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMarmitasScreen(onBack: () -> Unit = {}) {
    Scaffold(topBar = { TopAppBar(title = { Text("Gerenciar marmitas") }) }) { padding ->
        Column(Modifier.fillMaxSize().navigationBarsPadding().padding(padding)) {
            AdminMarmitasSection(visible = true)
        }
    }
}
