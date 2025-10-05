package com.saborpraticidade.marmita.ui.features.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import com.saborpraticidade.marmita.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.LazyPagingItems
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.ExperimentalMaterialApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    onCheckout: () -> Unit,
    onAuthClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onMarmitaClick: (String) -> Unit = {},
    isLoggedIn: Boolean = false,
    vm: HomeViewModel = koinViewModel()
) {
    val uiState by vm.uiState.collectAsState()
    val paging = uiState.marmitas?.collectAsLazyPagingItems()
    val refreshing = paging?.loadState?.refresh is androidx.paging.LoadState.Loading
    val pullState = rememberPullRefreshState(refreshing = refreshing ?: false, onRefresh = { paging?.refresh(); vm.refreshBanner(); vm.refreshLogo(); vm.refreshStoreName(); vm.refreshLogoSize() })
    val widthDp = LocalConfiguration.current.screenWidthDp
    val minCell = when {
        widthDp < 600 -> 200.dp
        widthDp < 840 -> 220.dp
        else -> 260.dp
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(8.dp))
                // Perfil / Login
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                    label = { Text(if (isLoggedIn) "Perfil" else "Entrar / Cadastrar") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (isLoggedIn) onProfileClick() else onAuthClick()
                    }
                )
                // Carrinho (mostra contagem quando houver)
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                    label = {
                        val count = uiState.cartCount
                        Text(if (count > 0) "Carrinho ($count)" else "Carrinho")
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onCheckout()
                    }
                )
                // Opções de admin
                if (uiState.isAdmin) {
                    HorizontalDivider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                        label = { Text("Atualizar banner") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            vm.refreshBanner()
                        }
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                        label = { Text("Atualizar logo") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            vm.refreshLogo()
                        }
                    )
                }
                if (isLoggedIn) {
                    HorizontalDivider()
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                        label = { Text("Sair") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogoutClick()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        // Title remains: logo + store name, with robust vertical centering
                        val barHeight = 64.dp
                        val maxLogoSize = barHeight - 16.dp
                        Row(
                            modifier = Modifier.height(barHeight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!uiState.logoUrl.isNullOrBlank()) {
                                val reqLogo = ImageRequest.Builder(LocalContext.current)
                                    .data(uiState.logoUrl)
                                    .crossfade(true)
                                    .build()
                                val configured = (uiState.logoSizeDp.coerceIn(16, 96)).dp
                                val size = minOf(configured, maxLogoSize)
                                AsyncImage(
                                    model = reqLogo,
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(size),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Text(
                                uiState.storeName,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.offset(y = 1.dp)
                            )
                        }
                    },
                    navigationIcon = {},
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
        Column(Modifier.padding(padding)) {
            // Hero banner com cantos arredondados e skeleton
            if (!uiState.bannerUrl.isNullOrBlank()) {
                val req = ImageRequest.Builder(LocalContext.current).data(uiState.bannerUrl).crossfade(true).build()
                AsyncImage(
                    model = req,
                    contentDescription = "Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Box(Modifier.pullRefresh(pullState)) {
                    LazyVerticalGrid(columns = GridCells.Adaptive(minCell), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (paging != null) items(paging.itemCount) { index ->
                            val item = paging[index] ?: return@items
                            ElevatedCard(onClick = { item.id?.let { onMarmitaClick(it) } }, shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth()) {
                                if (!item.image_url.isNullOrBlank()) {
                                    val request = ImageRequest.Builder(LocalContext.current)
                                        .data(item.image_url)
                                        .crossfade(true)
                                        .build()
                                    AsyncImage(
                                        model = request,
                                        contentDescription = item.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f/9f)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f/9f)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Column(Modifier.padding(12.dp)) {
                                    Text(item.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    val price = remember(item.price) {
                                        try {
                                            val fmt = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt","BR"))
                                            fmt.format(item.price)
                                        } catch (_: Exception) { "R$ %.2f".format(item.price) }
                                    }
                                    Text(price, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    FilledTonalButton(onClick = { vm.addToCart(item) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(999.dp)) {
                                        Text("Adicionar")
                                    }
                                }
                            }
                            }
                        }
                    }
                    PullRefreshIndicator(refreshing ?: false, pullState, Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}
}
