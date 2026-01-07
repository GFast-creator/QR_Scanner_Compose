package ru.gfastg98.qr_scanner_compose.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity
import ru.gfastg98.qr_scanner_compose.data.entity.QRCodeEntity.Companion.TABLE_NAME


@Dao
interface QRCodeDao {
    @Query("SELECT * FROM $TABLE_NAME")
    fun getAll(): List<QRCodeEntity>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllFlowed(): Flow<List<QRCodeEntity>>

    @Query("SELECT * FROM $TABLE_NAME WHERE uid IN (:userIds)")
    fun loadAllByIds(vararg userIds: Int): Flow<List<QRCodeEntity>>

    @Query("SELECT * FROM $TABLE_NAME WHERE generated IN (:generated)")
    fun getAllWithGenerated(generated: Boolean): Flow<List<QRCodeEntity>>

    @Insert
    suspend fun insertAll(vararg users: QRCodeEntity)

    @Delete
    suspend fun delete(user: QRCodeEntity)

    @Delete
    suspend fun deleteAll(user: List<QRCodeEntity>)
}