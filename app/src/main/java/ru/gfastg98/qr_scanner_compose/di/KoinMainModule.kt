package ru.gfastg98.qr_scanner_compose.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.domain.QRCodeDatabaseViewModel
import ru.gfastg98.qr_scanner_compose.domain.QRCodeGeneratorViewModel
import ru.gfastg98.qr_scanner_compose.domain.QRCodeResultViewModel
import ru.gfastg98.qr_scanner_compose.domain.QRCodeScannerViewModel

val mainModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "qr_code_database"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    viewModelOf(::QRCodeDatabaseViewModel)
    viewModelOf(::QRCodeGeneratorViewModel)
    viewModelOf(::QRCodeResultViewModel)
    viewModelOf(::QRCodeScannerViewModel)
}