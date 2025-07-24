package ru.gfastg98.qr_scanner_compose

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import ru.gfastg98.qr_scanner_compose.di.mainModule

class App : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(mainModule)
            workManagerFactory()
        }
    }
}