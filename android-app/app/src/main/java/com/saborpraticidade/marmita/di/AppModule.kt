package com.saborpraticidade.marmita.di

import com.saborpraticidade.marmita.BuildConfig
import com.saborpraticidade.marmita.ui.features.home.HomeViewModel
import com.saborpraticidade.marmita.data.repo.SupabaseRepository
import com.saborpraticidade.marmita.ui.features.order.CheckoutViewModel
import com.saborpraticidade.marmita.data.cart.CartRepository
import com.saborpraticidade.marmita.data.session.SessionManager
import com.saborpraticidade.marmita.data.auth.AuthRepository
import com.saborpraticidade.marmita.ui.features.auth.AuthViewModel
import com.saborpraticidade.marmita.data.geo.ViaCepService
import com.saborpraticidade.marmita.data.repo.ProfileRepository
import com.saborpraticidade.marmita.ui.features.profile.ProfileViewModel
import com.saborpraticidade.marmita.ui.features.details.MarmitaDetailsViewModel
import com.saborpraticidade.marmita.data.repo.AdminRepository
import com.saborpraticidade.marmita.ui.features.profile.AdminMarmitasViewModel
import com.saborpraticidade.marmita.data.repo.StorageRepository
import com.saborpraticidade.marmita.data.repo.SettingsRepository
import com.saborpraticidade.marmita.ui.features.orders.MyOrdersViewModel
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
        single {
            HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true; isLenient = true })
                }
            }
        }
    
        single { SupabaseRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY) }
    single { CartRepository() }

    // ViewModels
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { CheckoutViewModel(get(), get(), get()) }
    single { SessionManager(get()) }
    single { AuthRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, get()) }
    single { ViaCepService(get()) }
    viewModel { AuthViewModel(get(), get()) }
    single { ProfileRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, get()) }
    viewModel { ProfileViewModel(get()) }
    viewModel { MarmitaDetailsViewModel(get()) }
    single { AdminRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, get()) }
    viewModel { AdminMarmitasViewModel(get()) }
    single { StorageRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, get()) }
    single { SettingsRepository(get(), BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_KEY, get()) }
    viewModel { MyOrdersViewModel(get(), get()) }
}
