package com.mobdeve.s15.reyes.janicamegan.clospace

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        ClothingItem::class,
        Outfit::class,
        OutfitItem::class,
        CalendarEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class ClospaceDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun clothingDao(): ClothingDao

    abstract fun outfitDao(): OutfitDao

    abstract fun calendarDao(): CalendarDao

    companion object {

        @Volatile
        private var INSTANCE: ClospaceDatabase? = null

        fun getDatabase(context: Context): ClospaceDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClospaceDatabase::class.java,
                    "clospace_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}