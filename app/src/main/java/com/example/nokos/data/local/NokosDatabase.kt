package com.example.nokos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.nokos.data.model.NokosOrderEntity
import com.example.nokos.data.model.TransactionEntity

@Database(
    entities = [NokosOrderEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class NokosDatabase : RoomDatabase() {
    abstract fun nokosDao(): NokosDao

    companion object {
        @Volatile
        private var INSTANCE: NokosDatabase? = null

        fun getDatabase(context: Context): NokosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NokosDatabase::class.java,
                    "nokos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
