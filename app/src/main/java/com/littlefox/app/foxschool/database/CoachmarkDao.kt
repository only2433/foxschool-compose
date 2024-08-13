package com.littlefox.app.foxschool.database

import androidx.room.*



@Dao
interface CoachmarkDao
{
    @Query("SELECT * FROM coachMark")
    fun getAll() : List<CoachmarkEntity>

    @Query("SELECT * FROM coachMark WHERE user_id LIKE :userID LIMIT 1")
    fun getSavedCoachmarkUser(userID : String) : CoachmarkEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertItem(coachmarkEntity : CoachmarkEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateItem(coachmarkEntity : CoachmarkEntity)

    @Delete
    fun deleteItem(coachmarkEntity : CoachmarkEntity)
}