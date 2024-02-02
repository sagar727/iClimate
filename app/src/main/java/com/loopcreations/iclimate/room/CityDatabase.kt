package com.loopcreations.iclimate.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CityEntity::class], version = 1)
abstract class CityDatabase: RoomDatabase() {

    abstract fun cityDao():CityDao

    companion object{
        @Volatile
        private var INSTANCE: CityDatabase? = null

        fun getDatabase(context: Context): CityDatabase {
            synchronized(this){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.applicationContext,CityDatabase::class.java,"city_database").build()
                }
            }
            return INSTANCE!!
        }
    }
}