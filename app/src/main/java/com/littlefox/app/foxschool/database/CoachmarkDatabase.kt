package com.littlefox.app.foxschool.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CoachmarkEntity::class], version = 1)
abstract class CoachmarkDatabase : RoomDatabase()
{
    abstract fun coachmarkDao() : CoachmarkDao

    companion object
    {
        private var sInstance : CoachmarkDatabase? = null;

        @Synchronized
        fun getInstance(context : Context) : CoachmarkDatabase?
        {
            if(sInstance == null)
            {
                sInstance = Room.databaseBuilder(
                        context.applicationContext, CoachmarkDatabase::class.java, "coachmark.db").fallbackToDestructiveMigration().build();
            }

            return sInstance;
        }

        fun release()
        {
            sInstance = null;
        }

    }
}
