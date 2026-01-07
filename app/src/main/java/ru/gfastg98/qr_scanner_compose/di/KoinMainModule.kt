package ru.gfastg98.qr_scanner_compose.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ru.gfastg98.qr_scanner_compose.data.AppDatabase
import ru.gfastg98.qr_scanner_compose.presentation.generator.QRCodeGeneratorViewModel
import ru.gfastg98.qr_scanner_compose.presentation.main.MainScreenViewModel
import ru.gfastg98.qr_scanner_compose.presentation.qr_picker.QrPickerViewModel
import ru.gfastg98.qr_scanner_compose.presentation.qr_result.QrResultViewModel
import ru.gfastg98.qr_scanner_compose.presentation.scanner.QrScannerViewModel

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

    viewModelOf(::MainScreenViewModel)
    viewModelOf(::QRCodeGeneratorViewModel)
    viewModelOf(::QrResultViewModel)
    viewModelOf(::QrScannerViewModel)
    viewModelOf(::QrPickerViewModel)
}