package ru.gfastg98.qr_scanner_compose

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    object Contract {
        // Table contents are grouped together in an anonymous object.
        object QRCodeEntry : BaseColumns {
            const val TABLE_NAME = "QRCodeBase"
            val COLUMNS = mapOf(
                BaseColumns._ID to "INTEGER PRIMARY KEY",
                "bitmap" to "BLOB",
                "content" to "TEXT",
                "generated" to "BOOLEAN",
                "barcode_obj_js" to "TEXT",
                "code_format" to "INT"
            )
        }
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "QRCodesDataBase.db"

        private val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${Contract.QRCodeEntry.TABLE_NAME} (" +
                    Contract.QRCodeEntry.COLUMNS.entries.joinToString(separator = ",\n") {
                        "${it.key} ${it.value}"
                    } + ")"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${Contract.QRCodeEntry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}

fun SQLiteDatabase.query(
    table:String,
    columns: Array<String>,
    selection : String? = null,
    selectionArgs : Array<String>? = null,
    groupBy : String? = null,
    having : String? = null,
    orderBy : String? = null
) : Cursor = query(table, columns, selection, selectionArgs, groupBy, having, orderBy)