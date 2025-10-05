package com.saborpraticidade.marmita

import android.app.Application
import com.saborpraticidade.marmita.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MarmitaApp : Application() {
	override fun onCreate() {
		super.onCreate()
		startKoin {
			androidContext(this@MarmitaApp)
			modules(appModule)
		}
	}
}
