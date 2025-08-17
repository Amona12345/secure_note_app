package com.example.securenotes.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.securenotes.data.db.dao.NoteDao
import com.example.securenotes.data.db.entities.Note

@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(TypeConverter::class)

abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    " TABLE note ADD New COLUMN "
                )
            }
        }

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "secure_notes.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
