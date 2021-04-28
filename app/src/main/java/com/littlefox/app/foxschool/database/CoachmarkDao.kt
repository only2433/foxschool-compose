package com.littlefox.app.foxschool.database

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface CoachmarkDao
{
    @Query("SELECT * FROM coachMark")
    fun getAll() : List<CoachmarkEntity>

    @Query("SELECT * FROM coachMark WHERE user_id LIKE :userID LIMIT 1")
    fun getSavedCoachmarkUser(userID : String) : CoachmarkEntity

    @Insert(onConflict = REPLACE)
    fun insertItem(coachmarkEntity : CoachmarkEntity)

    @Update(onConflict = REPLACE)
    fun updateItem(coachmarkEntity : CoachmarkEntity)

    @Delete
    fun deleteItem(coachmarkEntity : CoachmarkEntity)
}