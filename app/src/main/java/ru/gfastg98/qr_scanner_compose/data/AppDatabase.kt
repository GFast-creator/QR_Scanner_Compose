package ru.gfastg98.qr_scanner_compose.data

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.gfastg98.qr_scanner_compose.data.dao.QRCodeDao
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity

@Database(entities = [QRCodeEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun qrCodeDao(): QRCodeDao
}