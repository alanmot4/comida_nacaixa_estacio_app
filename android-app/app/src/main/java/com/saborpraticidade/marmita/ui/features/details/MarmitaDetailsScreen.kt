package com.saborpraticidade.marmita.ui.features.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.cart.CartRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarmitaDetailsScreen(
    id: String,
    onBack: () -> Unit,
    vm: MarmitaDetailsViewModel = koinViewModel()
){
    val state by vm.ui.collectAsState()
    val cart: CartRepository = koinInject()

    LaunchedEffect(id) { vm.load(id) }

    Scaffold(topBar = { TopAppBar(title = { Text(state.item?.name ?: "Detalhes") }, navigationIcon = { 
        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar") }
    }) }) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.error!!) }
            state.item != null -> MarmitaDetailsContent(state.item!!, onAdd = { cart.add(it) }, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun MarmitaDetailsContent(item: Marmita, onAdd: (Marmita) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!item.image_url.isNullOrBlank()) {
            val request = ImageRequest.Builder(LocalContext.current).data(item.image_url).crossfade(true).build()
            AsyncImage(model = request, contentDescription = item.name, modifier = Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop)
        }
        Text(item.name, style = MaterialTheme.typography.headlineSmall)
        Text("R$ %.2f".format(item.price), style = MaterialTheme.typography.titleMedium)
    if (!item.description.isNullOrBlank()) Text(item.description!!)
    HorizontalDivider()
        Text("Ingredientes", style = MaterialTheme.typography.titleMedium)
        if (item.ingredients.isEmpty()) {
            Text("Sem ingredientes cadastrados")
        } else {
            item.ingredients.forEach { ing ->
                Text("- ${ing.name}: ${ing.grams}g")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { onAdd(item) }, modifier = Modifier.fillMaxWidth()) { Text("Adicionar ao carrinho") }
    }
}
