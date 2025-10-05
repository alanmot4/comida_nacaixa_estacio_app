package com.saborpraticidade.marmita.ui.features.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.saborpraticidade.marmita.data.repo.SettingsRepository
import com.saborpraticidade.marmita.data.repo.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCustomizeStoreScreen(onBack: () -> Unit = {}) {
    val settings: SettingsRepository = koinInject()
    val storage: StorageRepository = koinInject()
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bannerUrl by remember { mutableStateOf<String?>(null) }
    var logoUrl by remember { mutableStateOf<String?>(null) }
    var storeName by remember { mutableStateOf("") }
    var logoSize by remember { mutableStateOf(24) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loading = true; error = null
        runCatching { settings.getBannerUrl() }
            .onSuccess { bannerUrl = it }
            .onFailure { error = it.message }
        runCatching { settings.getLogoUrl() }
            .onSuccess { logoUrl = it }
            .onFailure { error = it.message }
        runCatching { settings.getStoreName() }
            .onSuccess { name -> storeName = name ?: storeName }
            .onFailure { error = it.message }
        runCatching { settings.getLogoSizeDp() }
            .onSuccess { s -> if (s != null) logoSize = s }
            .onFailure { error = it.message }
        loading = false
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                loading = true; error = null
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                }
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                runCatching { storage.uploadBanner(bytes, mime) }
                    .onSuccess { url ->
                        runCatching { settings.setBannerUrl(url) }
                            .onSuccess { bannerUrl = url }
                            .onFailure { error = it.message }
                    }
                    .onFailure { error = it.message }
                loading = false
            }
        }
    }

    val logoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                loading = true; error = null
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                }
                val mime = context.contentResolver.getType(uri) ?: "image/png"
                runCatching { storage.uploadLogo(bytes, mime) }
                    .onSuccess { url ->
                        runCatching { settings.setLogoUrl(url) }
                            .onSuccess { logoUrl = url }
                            .onFailure { error = it.message }
                    }
                    .onFailure { error = it.message }
                loading = false
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Personalizar loja") }) }) { padding ->
        val scroll = rememberScrollState()
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("Banner principal", style = MaterialTheme.typography.titleMedium)
            Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                if (!bannerUrl.isNullOrBlank()) {
                    val req = ImageRequest.Builder(LocalContext.current).data(bannerUrl).crossfade(true).build()
                    AsyncImage(model = req, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Text("Nenhum banner definido")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { imagePicker.launch("image/*") }, enabled = !loading) { Text("Selecionar banner") }
            }
            Divider()
            Text("Nome da loja", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = storeName, onValueChange = { storeName = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Ex.: Sabor & Praticidade") })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    scope.launch {
                        loading = true; error = null
                        runCatching { settings.setStoreName(storeName) }
                            .onFailure { error = it.message }
                        loading = false
                    }
                }, enabled = !loading && storeName.isNotBlank()) { Text("Salvar nome") }
            }
            Divider()
            Text("Logo", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { logoPicker.launch("image/*") }, enabled = !loading) { Text("Selecionar logo") }
                    Button(onClick = {
                        scope.launch { loading = true; error = null; runCatching { settings.setLogoSizeDp(logoSize) }.onFailure { error = it.message }; loading = false }
                    }, enabled = !loading) { Text("Salvar tamanho") }
                }
                Text("Tamanho da logo: ${'$'}logoSize dp")
                Slider(value = logoSize.toFloat(), onValueChange = { logoSize = it.toInt() }, valueRange = 16f..96f, steps = 96 - 16 - 1)
                // Live preview similar to Home top bar
                Surface(tonalElevation = 1.dp, shadowElevation = 1.dp) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!logoUrl.isNullOrBlank()) {
                            val req = ImageRequest.Builder(LocalContext.current).data(logoUrl).crossfade(true).build()
                            AsyncImage(model = req, contentDescription = null, modifier = Modifier.size(logoSize.dp))
                        } else {
                            Box(Modifier.size(logoSize.dp), contentAlignment = Alignment.Center) { Text("Logo") }
                        }
                        Text(storeName.ifBlank { "Sabor & Praticidade" }, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}
