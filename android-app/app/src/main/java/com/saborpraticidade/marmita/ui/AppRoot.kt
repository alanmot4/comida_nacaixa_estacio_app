package com.saborpraticidade.marmita.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.saborpraticidade.marmita.ui.features.home.HomeScreen
import com.saborpraticidade.marmita.ui.features.auth.AuthScreen
import com.saborpraticidade.marmita.ui.features.auth.AuthViewModel
import com.saborpraticidade.marmita.ui.features.profile.ProfileScreen
import com.saborpraticidade.marmita.ui.features.profile.ProfileViewModel
import com.saborpraticidade.marmita.data.session.SessionManager
import org.koin.compose.koinInject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.compose.getViewModel
import androidx.compose.foundation.layout.padding
import com.saborpraticidade.marmita.ui.features.order.CheckoutScreen
import com.saborpraticidade.marmita.ui.features.admin.AdminMarmitasScreen
import com.saborpraticidade.marmita.ui.features.admin.AdminOrdersScreen
import com.saborpraticidade.marmita.ui.features.admin.AdminCustomizeStoreScreen
import com.saborpraticidade.marmita.ui.features.orders.MyOrdersScreen

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    // Brazilian-inspired color scheme
    val colorScheme = lightColorScheme(
        primary = Color(0xFFFF6333), // Laranja vibrante
        onPrimary = Color.White,
        secondary = Color(0xFF00A859), // Verde bandeira
        onSecondary = Color.White,
        tertiary = Color(0xFFFFDF00), // Amarelo bandeira
        onTertiary = Color(0xFF333333)
    )

    MaterialTheme(colorScheme = colorScheme) {
        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute?.startsWith("home") == true,
                        onClick = {
                            if (currentRoute != "home") {
                                navController.navigate("home") {
                                    launchSingleTop = true
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Início") },
                        label = { Text("Início") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "checkout",
                        onClick = {
                            if (currentRoute != "checkout") {
                                navController.navigate("checkout") { launchSingleTop = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrinho") },
                        label = { Text("Carrinho") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == "profile",
                        onClick = {
                            if (currentRoute != "profile") {
                                navController.navigate("profile") { launchSingleTop = true }
                            }
                        },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
            composable("home") {
                val session: SessionManager = koinInject()
                val loggedIn by produceState(initialValue = false, session) {
                    value = session.getToken() != null
                }
                val scope = rememberCoroutineScope()
                HomeScreen(
                    onCheckout = { navController.navigate("checkout") },
                    onAuthClick = { navController.navigate("auth") },
                    onProfileClick = { navController.navigate("profile") },
                    onLogoutClick = { scope.launch { session.clear() } },
                    onMarmitaClick = { id -> navController.navigate("marmita/$id") },
                    isLoggedIn = loggedIn
                )
            }
            composable("checkout") { CheckoutScreen(onDone = { navController.popBackStack() }) }
            composable(
                route = "marmita/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: return@composable
                com.saborpraticidade.marmita.ui.features.details.MarmitaDetailsScreen(id = id, onBack = { navController.popBackStack() })
            }
            composable("auth") {
                val vm: AuthViewModel = koinViewModel()
                val state = vm.uiState.collectAsState().value
                AuthScreen(
                    state = state,
                    onEmailChange = vm::onEmailChange,
                    onPasswordChange = vm::onPasswordChange,
                    onFullNameChange = vm::onFullNameChange,
                    onPhoneChange = vm::onPhoneChange,
                    onCepChange = vm::onCepChange,
                    onStreetChange = vm::onStreetChange,
                    onNumberChange = vm::onNumberChange,
                    onComplementChange = vm::onComplementChange,
                    onNeighborhoodChange = vm::onNeighborhoodChange,
                    onCityChange = vm::onCityChange,
                    onStateUfChange = vm::onStateUfChange,
                    onSignIn = { vm.signIn { navController.popBackStack() } },
                    onSignUp = { vm.signUp { navController.popBackStack() } }
                )
            }
            composable("profile") {
                val vm: ProfileViewModel = koinViewModel()
                val state = vm.uiState.collectAsState().value
                ProfileScreen(
                    state = state,
                    onFullNameChange = vm::onFullNameChange,
                    onPhoneChange = vm::onPhoneChange,
                    onSave = vm::save,
                    onOpenAdmin = { navController.navigate("admin/marmitas") },
                    onOpenAdminOrders = { navController.navigate("admin/pedidos") },
                    onOpenCustomize = { navController.navigate("admin/customize") },
                    onOpenMyOrders = { navController.navigate("orders/mine") }
                )
            }
            composable("orders/mine") { MyOrdersScreen(onBack = { navController.popBackStack() }) }
            composable("admin/marmitas") { AdminMarmitasScreen(onBack = { navController.popBackStack() }) }
            composable("admin/pedidos") { AdminOrdersScreen(onBack = { navController.popBackStack() }) }
            composable("admin/customize") { AdminCustomizeStoreScreen(onBack = { navController.popBackStack() }) }
        }
        }
    }
}

@Preview
@Composable
private fun AppRootPreview() {
    AppRoot()
}
