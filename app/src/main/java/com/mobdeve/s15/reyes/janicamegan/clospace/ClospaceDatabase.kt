package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        User::class,
        ClothingItem::class,
        Outfit::class,
        OutfitItem::class,
        CalendarEntry::class
    ],
    version = 6,
    exportSchema = false
)
abstract class ClospaceDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun clothingDao(): ClothingDao

    abstract fun outfitDao(): OutfitDao

    abstract fun calendarDao(): CalendarDao

    companion object {

        private val MIGRATION_5_6 = object : Migration(5, 6) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    "ALTER TABLE clothing_items ADD COLUMN material TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        @Volatile
        private var INSTANCE: ClospaceDatabase? = null

        fun getDatabase(context: Context): ClospaceDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClospaceDatabase::class.java,
                    "clospace_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}