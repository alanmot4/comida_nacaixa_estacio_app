package com.saborpraticidade.marmita.ui.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saborpraticidade.marmita.data.Marmita
import com.saborpraticidade.marmita.data.MarmitaIngredient
import com.saborpraticidade.marmita.data.repo.AdminRepository
import com.saborpraticidade.marmita.data.repo.StorageRepository
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn

data class AdminMarmitasState(
    val loading: Boolean = false,
    val items: List<Marmita> = emptyList(),
    val error: String? = null,
)

class AdminMarmitasViewModel(private val repo: AdminRepository): androidx.lifecycle.ViewModel() {
    var state by mutableStateOf(AdminMarmitasState())
        private set

    fun load() {
        viewModelScope.launch {
            state = state.copy(loading = true, error = null)
            runCatching { repo.listAllMarmitas() }
                .onSuccess { state = state.copy(loading = false, items = it) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun createEmpty() {
        val m = Marmita(name = "Nova marmita", description = "", price = 0.0, available = true, image_url = null, category_id = null, ingredients = emptyList())
        viewModelScope.launch {
            runCatching { repo.createMarmita(m) }.onSuccess { load() }
        }
    }

    fun update(item: Marmita) {
        val id = item.id ?: return
        viewModelScope.launch {
            runCatching { repo.updateMarmita(id, item) }.onSuccess { load() }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            runCatching { repo.deleteMarmita(id) }.onSuccess { load() }
        }
    }

    fun create(item: Marmita) {
        viewModelScope.launch {
            runCatching { repo.createMarmita(item) }.onSuccess { load() }
        }
    }
}

@Composable
fun AdminMarmitasSection(visible: Boolean, vm: AdminMarmitasViewModel = koinViewModel()) {
    if (!visible) return
    LaunchedEffect(Unit) { vm.load() }
    var showDialog by remember { mutableStateOf<Marmita?>(null) }
    Column(Modifier.fillMaxSize().padding(top = 24.dp)) {
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Gerenciar marmitas", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showDialog = Marmita(name = "", description = "", price = 0.0, available = true, image_url = null, category_id = null, ingredients = emptyList()) }) { Text("Nova") }
            }
            if (vm.state.loading) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            vm.state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(vm.state.items, key = { it.id ?: it.name }) { item ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name.ifBlank { "(sem nome)" }, style = MaterialTheme.typography.titleMedium)
                            Text("R$ %.2f".format(item.price), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { showDialog = item }) { Text("Editar") }
                            TextButton(onClick = { item.id?.let(vm::delete) }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }

    val editing = showDialog
    if (editing != null) {
        AlertDialog(
            onDismissRequest = { showDialog = null },
            confirmButton = {},
            title = { Text(if (editing.id == null) "Nova marmita" else "Editar marmita") },
            text = {
                // Conteúdo rolável dentro do diálogo para caber em telas menores
                Column(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 4.dp)
                ) {
                    AdminMarmitaItem(
                        item = editing,
                        onChange = {
                            if (editing.id == null) vm.create(it) else vm.update(it)
                            showDialog = null
                        },
                        onDelete = {
                            editing.id?.let { vm.delete(it); showDialog = null }
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun AdminMarmitaItem(item: Marmita, onChange: (Marmita) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(item.name) }
    var priceText by remember { mutableStateOf(item.price.toString()) }
    var imageUrl by remember { mutableStateOf(item.image_url ?: "") }
    var description by remember { mutableStateOf(item.description ?: "") }
    var available by remember { mutableStateOf(item.available == true) }
    var ingredients by remember { mutableStateOf(item.ingredients) }
    val storage: StorageRepository = koinInject()
    val context = LocalContext.current
    var uploading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Read bytes off main thread and upload
            val cr = context.contentResolver
            uploading = true
            scope.launch {
                val bytes = withContext(Dispatchers.IO) {
                    cr.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                }
                val mime = cr.getType(uri) ?: "image/jpeg"
                runCatching { storage.uploadImageToMarmitas(bytes, mime) }
                    .onSuccess { url -> imageUrl = url }
                    .onFailure { /* TODO: surface error */ }
                uploading = false
            }
        }
    }

    fun emitChange() {
        val price = priceText.toDoubleOrNull() ?: 0.0
        onChange(item.copy(name = name, price = price, image_url = imageUrl.ifBlank { null }, description = description.ifBlank { null }, available = available, ingredients = ingredients))
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Preço") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Imagem URL") }, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { imagePicker.launch("image/*") }, enabled = !uploading) { Text(if (uploading) "Enviando..." else "Selecionar") }
            }
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth())
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = available, onCheckedChange = { available = it })
                Spacer(Modifier.width(8.dp))
                Text(if (available) "Disponível" else "Indisponível")
            }
            Text("Ingredientes", style = MaterialTheme.typography.titleSmall)
            ingredients.forEachIndexed { idx, ing ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ing.name,
                        onValueChange = { val list = ingredients.toMutableList(); list[idx] = ing.copy(name = it); ingredients = list },
                        label = { Text("Nome") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = ing.grams.toString(),
                        onValueChange = { val g = it.toIntOrNull() ?: 0; val list = ingredients.toMutableList(); list[idx] = ing.copy(grams = g); ingredients = list },
                        label = { Text("g") },
                        modifier = Modifier.width(100.dp)
                    )
                    TextButton(onClick = { ingredients = ingredients.toMutableList().also { l -> l.removeAt(idx) } }) { Text("Remover") }
                }
            }
            TextButton(onClick = { ingredients = ingredients + MarmitaIngredient(name = "", grams = 0) }) { Text("Adicionar ingrediente") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { emitChange() }) { Text("Salvar") }
                OutlinedButton(onClick = onDelete, enabled = item.id != null) { Text("Excluir") }
            }
        }
    }
}
